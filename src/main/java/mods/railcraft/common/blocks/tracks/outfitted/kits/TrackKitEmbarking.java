/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2016
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.blocks.tracks.outfitted.kits;

import mods.railcraft.api.core.items.IToolCrowbar;
import mods.railcraft.common.blocks.tracks.outfitted.TrackKits;
import mods.railcraft.common.carts.CartTools;
import mods.railcraft.common.gui.EnumGui;
import mods.railcraft.common.gui.GuiHandler;
import mods.railcraft.common.util.effects.EffectManager;
import mods.railcraft.common.util.inventory.InvTools;
import mods.railcraft.common.util.misc.AABBFactory;
import mods.railcraft.common.util.misc.MiscTools;
import mods.railcraft.common.util.network.IGuiReturnHandler;
import mods.railcraft.common.util.network.RailcraftInputStream;
import mods.railcraft.common.util.network.RailcraftOutputStream;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TrackKitEmbarking extends TrackKitPowered implements IGuiReturnHandler {

    public static final Set<Class> excludedEntities = new HashSet<Class>();
    public static final byte MIN_AREA = 1;
    public static final byte MAX_AREA = 5;

    static {
        excludedEntities.add(EntityIronGolem.class);
        excludedEntities.add(EntityDragon.class);
        excludedEntities.add(EntityWither.class);
        excludedEntities.add(EntityBlaze.class);
        excludedEntities.add(EntityMagmaCube.class);
        excludedEntities.add(EntitySquid.class);
        excludedEntities.add(EntityBat.class);
    }

    private byte area = 2;

    @Nullable
    @Override
    public World theWorld() {
        return super.theWorld();
    }

    @Override
    public TrackKits getTrackKitContainer() {
        return TrackKits.EMBARKING;
    }

    @Override
    public boolean blockActivated(EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem) {
        if (!InvTools.isEmpty(heldItem) && heldItem.getItem() instanceof IToolCrowbar) {
            IToolCrowbar crowbar = (IToolCrowbar) heldItem.getItem();
            GuiHandler.openGui(EnumGui.TRACK_EMBARKING, player, theWorldAsserted(), getPos());
            crowbar.onWhack(player, hand, heldItem, getPos());
            return true;
        }
        return false;
    }

    @Override
    public void onMinecartPass(EntityMinecart cart) {
        if (isPowered() && cart.canBeRidden() && !cart.isBeingRidden() && cart.getEntityData().getInteger("MountPrevention") <= 0) {
            int a = area;
            AxisAlignedBB box = AABBFactory.start().createBoxForTileAt(getPos()).build();
            box = box.expand(a, a, a);
            List<EntityLivingBase> entities = theWorldAsserted().getEntitiesWithinAABB(EntityLivingBase.class, box);

            if (entities.size() > 0) {
                EntityLivingBase entity = entities.get(MiscTools.RANDOM.nextInt(entities.size()));

                if (entity instanceof EntityPlayer) {
                    EntityPlayer player = (EntityPlayer) entity;
                    if (player.isSneaking()) {
                        return;
                    }

                    ItemStack current = player.getActiveItemStack();
                    if (!InvTools.isEmpty(current) && current.getItem() instanceof IToolCrowbar) {
                        return;
                    }
                } else if (excludedEntities.contains(entity.getClass())) {
                    return;
                } else if (entity instanceof EntitySlime) {
                    EntitySlime slime = (EntitySlime) entity;
                    if (slime.getSlimeSize() >= 100)
                        return;
                }

                if (!entity.isRiding()) {
                    EffectManager.instance.teleportEffect(entity, cart.getPositionVector());
                    CartTools.addPassenger(cart, entity);
                }
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setByte("area", area);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        area = data.getByte("area");
    }

    @Override
    public void writePacketData(DataOutputStream data) throws IOException {
        super.writePacketData(data);
        data.writeByte(area);
    }

    @Override
    public void readPacketData(DataInputStream data) throws IOException {
        super.readPacketData(data);
        setArea(data.readByte());
    }

    @Override
    public void writeGuiData(RailcraftOutputStream data) throws IOException {
        data.writeByte(area);
    }

    @Override
    public void readGuiData(RailcraftInputStream data, EntityPlayer sender) throws IOException {
        byte a = data.readByte();
        if (area != a) {
            setArea(a);
            sendUpdateToClient();
        }
    }

    public byte getArea() {
        return area;
    }

    public void setArea(byte radius) {
        radius = (byte) Math.max(MIN_AREA, radius);
        radius = (byte) Math.min(MAX_AREA, radius);
        area = radius;
    }

}
