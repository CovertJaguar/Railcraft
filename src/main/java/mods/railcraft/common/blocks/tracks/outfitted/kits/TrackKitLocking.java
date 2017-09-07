/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2017
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.blocks.tracks.outfitted.kits;

import mods.railcraft.api.carts.CartToolsAPI;
import mods.railcraft.api.core.items.IToolCrowbar;
import mods.railcraft.api.events.CartLockdownEvent;
import mods.railcraft.api.tracks.ITrackKitLockdown;
import mods.railcraft.api.tracks.ITrackKitPowered;
import mods.railcraft.common.blocks.tracks.TrackShapeHelper;
import mods.railcraft.common.blocks.tracks.TrackTools;
import mods.railcraft.common.blocks.tracks.outfitted.TrackKits;
import mods.railcraft.common.blocks.tracks.outfitted.kits.locking.BoardingLockingProfile;
import mods.railcraft.common.blocks.tracks.outfitted.kits.locking.HoldingLockingProfile;
import mods.railcraft.common.blocks.tracks.outfitted.kits.locking.LockdownLockingProfile;
import mods.railcraft.common.blocks.tracks.outfitted.kits.locking.LockingProfile;
import mods.railcraft.common.carts.CartTools;
import mods.railcraft.common.carts.Train;
import mods.railcraft.common.plugins.forge.ChatPlugin;
import mods.railcraft.common.plugins.forge.LocalizationPlugin;
import mods.railcraft.common.plugins.forge.NBTPlugin;
import mods.railcraft.common.util.inventory.InvTools;
import mods.railcraft.common.util.misc.Game;
import net.minecraft.block.BlockRailBase;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.BitSet;
import java.util.List;
import java.util.UUID;

/**
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public class TrackKitLocking extends TrackKitRailcraft implements ITrackKitLockdown, ITrackKitPowered {
    public static double START_BOOST = 0.04;
    public static double BOOST_FACTOR = 0.06;
    private LockingProfileType profile = LockingProfileType.LOCKDOWN;
    private LockingProfile profileInstance = profile.create(this);
    @Nullable
    private EntityMinecart currentCart;
    @Nullable
    private EntityMinecart prevCart;
    @Nullable
    private Train currentTrain;
    private UUID uuid;
    private boolean trainLeaving;
    private boolean redstone;
    private boolean locked;
    private int trainDelay;

    @Override
    public TrackKits getTrackKitContainer() {
        return TrackKits.LOCKING;
    }

    @Override
    public int getRenderState() {
        int state = profile.ordinal();
        if (!locked)
            state += LockingProfileType.VALUES.length;
        return state;
    }

    public LockingProfileType getProfileType() {
        return profile;
    }

    public void setProfile(LockingProfileType type) {
        if (profile == type)
            return;
        profile = type;
        profileInstance = profile.create(this);
        if (Game.isHost(theWorldAsserted()))
            sendUpdateToClient();
    }

    /**
     * We try to calculate all the logic here so we can isolate it on the server side.
     * Its a bit tricky to determine whether a cart or train is on top of us, but we can do it
     * if we maintain a record of the last cart that was on us and the next cart that triggers
     * an onMinecartPass() event.
     */
    @Override
    public void update() {
        World world = theWorldAsserted();
        if (Game.isHost(world)) {
            boolean updateClient = false; // flag determines whether we send an update to the client, only update when visible changes occur

            if (currentCart != null && !currentCart.isEntityAlive()) {
                releaseCurrentCart();
                currentCart = null;
                updateClient = true;
            }
            boolean oldLocked = locked; // simple check to determine if "locked" has changed
            calculateLocked();
            if (oldLocked != locked)
                updateClient = true;

            if (locked) {
                lockCurrentCart();
            } else {
                releaseCurrentCart();
            }

            // Store our last found cart in prevCart
            if (currentCart != null)
                prevCart = currentCart;
            currentCart = null; // reset currentCart so we know if onMinecartPass() actually found one

            if (updateClient)
                sendUpdateToClient();

            markDirty();
        }
    }

    @Override
    public boolean blockActivated(EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem) {
        if (!InvTools.isEmpty(heldItem) && heldItem.getItem() instanceof IToolCrowbar) {
            IToolCrowbar crowbar = (IToolCrowbar) heldItem.getItem();
            if (crowbar.canWhack(player, hand, heldItem, getPos())) {
                LockingProfileType p;
                if (player.isSneaking())
                    p = profile.previous();
                else
                    p = profile.next();
                crowbar.onWhack(player, hand, heldItem, getPos());
                if (Game.isHost(theWorldAsserted()))
                    setProfile(p);
                else
                    ChatPlugin.sendLocalizedChat(player, "gui.railcraft.track.mode.change", "\u00A75" + LocalizationPlugin.translate("gui.railcraft.track.locking.mode." + p.tag));
                return true;
            }
        }
        return false;
    }

    @Override
    public void onBlockRemoved() {
        super.onBlockRemoved();
        releaseCart(); // Release any carts still holding on
    }

    private UUID getUUID() {
        if (uuid == null)
            uuid = UUID.randomUUID();
        return uuid;
    }

    private void lockCurrentCart() {
        if (currentCart != null) {
            Train train = Train.getTrain(currentCart);
            if (currentTrain != train && currentTrain != null)
                currentTrain.removeLockingTrack(getUUID());
            currentTrain = train;
            currentTrain.addLockingTrack(getUUID());
            MinecraftForge.EVENT_BUS.post(new CartLockdownEvent.Lock(currentCart, getPos()));
            profileInstance.onLock(currentCart);
            currentCart.motionX = 0.0D;
            currentCart.motionZ = 0.0D;
            BlockRailBase.EnumRailDirection trackShape = getRailDirectionRaw();
            if (TrackShapeHelper.isNorthSouth(trackShape))
                currentCart.posZ = getTile().getPos().getZ() + 0.5D;
            else
                currentCart.posX = getTile().getPos().getX() + 0.5D;
        }
    }

    @Override
    public void onMinecartPass(EntityMinecart cart) {
        currentCart = cart;
        profileInstance.onPass(currentCart);
    }

    private void releaseCurrentCart() {
        if (currentTrain != null)
            currentTrain.removeLockingTrack(getUUID());
        if (currentCart != null) {
            MinecraftForge.EVENT_BUS.post(new CartLockdownEvent.Release(currentCart, getPos()));
            profileInstance.onRelease(currentCart);
        }
    }

    @Override
    public void releaseCart() {
        trainLeaving = true;
    }

    @Override
    public boolean isCartLockedDown(EntityMinecart cart) {
        return locked && prevCart == cart;
    }

    /**
     * Determines if the current train is the same train or cart (depending on track type)
     * as the train or cart in previous ticks. The <code>trainDelay</code> is needed because there are
     * gaps between carts in a train where onMinecartPass() doesn't get called even though
     * the train is still passing over us.
     *
     * @return whether the current cart or train (depending on LockType) is the same as previous cart or trains
     */
    private boolean isSameTrainOrCart() {
        if (profile.lockType == LockType.TRAIN) {
            if (currentCart != null) {
                if (Train.areInSameTrain(currentCart, prevCart))
                    trainDelay = TrackTools.TRAIN_LOCKDOWN_DELAY; // reset trainDelay
                else
                    trainDelay = 0; // We've encountered a new train, force the delay to 0 so we return false
            } else if (trainLeaving) {
                List<EntityMinecart> carts = CartToolsAPI.getMinecartsAt(theWorldAsserted(), getPos(), 0.0f);
                for (EntityMinecart cart : carts) {
                    if (Train.areInSameTrain(cart, prevCart)) {
                        trainDelay = TrackTools.TRAIN_LOCKDOWN_DELAY;
                        break;
                    }
                }
            }

            if (trainDelay > 0)
                trainDelay--;
            else
                prevCart = null;
            return trainDelay > 0;
        } else {
            return currentCart != null &&
                    (profile.lockType == LockType.CART && currentCart == prevCart);
        }
    }

    /**
     * The heart of the logic for this class is done here. If you understand what's going
     * on here, the rest will make much more sense to you. Basically, we're trying to determine
     * whether this track should be trying to lock the current or next cart that passes over it.
     * First of all we must realize that we only have 2 inputs: 1) whether a train/cart
     * is passing over us and 2) whether our track is receiving a redstone signal. If we try to
     * create a truth table with 2 boolean inputs to calculate "locked", we find that we can't quite
     * express the correct value for "locked". When we analyze the situation, we notice that when
     * a train is passing over the track, we need both the redstone to be off and the last cart to be
     * off the track in order to lock the track. However after the train has already left the track,
     * then we want the track to be "locked" when the redstone is off, regardless of whether a
     * new or old cart starts moving onto the track. In the end, what we're really after is
     * having 2 truth tables and a way to decide which of the 2 tables to use. To do this, we
     * use the boolean <code>trainLeaving</code> to indicate which table to use. As the name
     * implies, <code>trainLeaving</code> indicates whether the train or cart is in the process
     * of leaving the track.
     */
    private void calculateLocked() {
        boolean isSameCart = isSameTrainOrCart();
        if (trainLeaving) {
            if (!isSameCart && !redstone) {
                // When the train is in the process of leaving, we know that the "trainLeaving" state ends
                // when both the carts and redstone signal are false
                trainLeaving = false;
            }
            locked = !(isSameCart || redstone);
        } else {
            if (isSameCart && redstone) { // When we get both signals we know a train is leaving, so we set the state as so
                trainLeaving = true;
            }
            locked = !redstone;
        }
    }

    @Override
    public boolean isPowered() {
        return redstone; // Why call this "redstone" instead of "powered"? Powered gives the impression that
        // the track will accelerate or unlock a cart and for this track we cannot assume that
        // having a redstone signal applied is equivalent to being powered. Based on the usage
        // of the isPowered()/setPowered() calls, it seems that they more accurately describe
        // whether a redstone signal is being applied. I have refrained from using "powered" in the code
        // in the hopes that the logic is easier to understand.
    }

    @Override
    public void setPowered(boolean powered) {
        this.redstone = powered;
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setByte("profile", (byte) profile.ordinal());
        profileInstance.writeToNBT(data);
        data.setBoolean("powered", redstone);
        data.setBoolean("locked", locked);
        data.setBoolean("trainLeaving", trainLeaving);
        data.setInteger("trainDelay", trainDelay);
        if (prevCart != null)
            NBTPlugin.writeUUID(data, "prevCart", prevCart.getPersistentID());
        if (currentCart != null)
            NBTPlugin.writeUUID(data, "currentCart", currentCart.getPersistentID());

        NBTPlugin.writeUUID(data, "uuid", getUUID());
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        if (data.hasKey("profile"))
            profile = LockingProfileType.fromOrdinal(data.getByte("profile"));
        profileInstance = profile.create(this);
        profileInstance.readFromNBT(data);
        redstone = data.getBoolean("powered");

        if (data.hasKey("locked"))
            locked = data.getBoolean("locked");

        if (data.hasKey("trainLeaving"))
            trainLeaving = data.getBoolean("trainLeaving");

        if (data.hasKey("trainDelay"))
            trainDelay = data.getInteger("trainDelay");

        prevCart = CartTools.getCartFromUUID(theWorld(), NBTPlugin.readUUID(data, "prevCart"));
        currentCart = CartTools.getCartFromUUID(theWorld(), NBTPlugin.readUUID(data, "currentCart"));

        uuid = NBTPlugin.readUUID(data, "uuid");
    }

    @Override
    public void writePacketData(DataOutputStream data) throws IOException {
        super.writePacketData(data);

        data.writeByte(profile.ordinal());
        BitSet bits = new BitSet();
        bits.set(0, redstone);
        bits.set(1, locked);
        data.write(bits.toByteArray());

        profileInstance.writePacketData(data);
    }

    @Override
    public void readPacketData(DataInputStream data) throws IOException {
        super.readPacketData(data);

        LockingProfileType p = LockingProfileType.fromOrdinal(data.readByte());
        if (profile != p) {
            profile = p;
            profileInstance = p.create(this);
        }
        BitSet bits = BitSet.valueOf(new byte[]{data.readByte()});
        redstone = bits.get(0);
        locked = bits.get(1);
        profileInstance.readPacketData(data);

        markBlockNeedsUpdate();
    }

    @Override
    public void markDirty() {
        theWorldAsserted().markChunkDirty(getPos(), null);
    }

    private enum LockType {

        CART, TRAIN
    }

    public enum LockingProfileType implements IStringSerializable {

        LOCKDOWN(LockdownLockingProfile.class, LockType.CART, "lockdown"),
        LOCKDOWN_TRAIN(LockdownLockingProfile.class, LockType.TRAIN, "lockdown.train"),
        HOLDING(HoldingLockingProfile.class, LockType.CART, "holding"),
        HOLDING_TRAIN(HoldingLockingProfile.class, LockType.TRAIN, "holding.train"),
        BOARDING_A(BoardingLockingProfile.class, LockType.CART, "boarding"),
        BOARDING_B(BoardingLockingProfile.class, LockType.CART, "boarding"),
        BOARDING_A_TRAIN(BoardingLockingProfile.class, LockType.TRAIN, "boarding.train"),
        BOARDING_B_TRAIN(BoardingLockingProfile.class, LockType.TRAIN, "boarding.train");
        public static final LockingProfileType[] VALUES = values();
        public final LockType lockType;
        public final String tag;
        private final MethodHandle constructor;

        @SuppressWarnings("JavaReflectionMemberAccess")
        LockingProfileType(Class<? extends LockingProfile> profileClass, LockType lockType, String tag) {
            try {
                this.constructor = MethodHandles.lookup().unreflectConstructor(profileClass.getConstructor(TrackKitLocking.class));
            } catch (NoSuchMethodException | IllegalAccessException e) {
                Game.logThrowable("Error initializing locking track kit!", e);
                throw new RuntimeException(e);
            }
            this.lockType = lockType;
            this.tag = tag;
        }

        public static LockingProfileType fromOrdinal(int ordinal) {
            if (ordinal < 0 || ordinal >= VALUES.length)
                return LOCKDOWN;
            return VALUES[ordinal];
        }

        public LockingProfileType next() {
            return VALUES[(ordinal() + 1) % VALUES.length];
        }

        public LockingProfileType previous() {
            return VALUES[(ordinal() + VALUES.length - 1) % VALUES.length];
        }

        public LockingProfile create(TrackKitLocking track) {
            try {
                return (LockingProfile) constructor.invoke(track);
            } catch (Throwable ex) {
                Game.logThrowable("Failed to create Locking Profile!", 10, ex);
                throw new RuntimeException(ex);
            }
        }

        @Override
        public String getName() {
            return tag;
        }
    }
}
