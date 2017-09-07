/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2017
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.carts;

import com.mojang.authlib.GameProfile;
import mods.railcraft.api.carts.CartToolsAPI;
import mods.railcraft.api.carts.IMinecart;
import mods.railcraft.api.core.RailcraftConstantsAPI;
import mods.railcraft.api.core.RailcraftFakePlayer;
import mods.railcraft.api.core.items.IMinecartItem;
import mods.railcraft.common.blocks.tracks.TrackTools;
import mods.railcraft.common.plugins.forge.PlayerPlugin;
import mods.railcraft.common.util.inventory.InvTools;
import mods.railcraft.common.util.inventory.wrappers.IInventoryComposite;
import mods.railcraft.common.util.misc.Game;
import mods.railcraft.common.util.misc.MiscTools;
import mods.railcraft.common.util.misc.Predicates;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class CartTools {

    public static Map<Item, IRailcraftCartContainer> vanillaCartItemMap = new HashMap<Item, IRailcraftCartContainer>();
    public static Map<Class<? extends Entity>, IRailcraftCartContainer> classReplacements = new HashMap<Class<? extends Entity>, IRailcraftCartContainer>();
    public static String HIGH_SPEED_TAG = "HighSpeed";

    /**
     * Spawns a new cart entity using the provided item.
     * <p/>
     * The backing item must implement <code>IMinecartItem</code> and/or extend
     * <code>ItemMinecart</code>.
     * <p/>
     * Generally Forge requires all cart items to extend ItemMinecart.
     *
     * @param owner The player name that should used as the owner
     * @param cart  An ItemStack containing a cart item, will not be changed by
     *              the function
     * @param world The World object
     * @return the cart placed or null if failed
     * @see IMinecartItem , ItemMinecart
     */
    @Nullable
    public static EntityMinecart placeCart(GameProfile owner, ItemStack cart, WorldServer world, BlockPos pos) {
        if (InvTools.isEmpty(cart))
            return null;
        cart = cart.copy();

        IRailcraftCartContainer vanillaType = vanillaCartItemMap.get(cart.getItem());
        if (vanillaType != null)
            return placeCart(vanillaType, owner, cart, world, pos);

        return CartToolsAPI.placeCart(owner, cart, world, pos);
    }

    @Nullable
    public static EntityMinecart placeCart(IRailcraftCartContainer cartType, GameProfile owner, ItemStack cartStack, World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        if (TrackTools.isRailBlock(state))
            if (!CartToolsAPI.isMinecartAt(world, pos, 0)) {
                BlockRailBase.EnumRailDirection trackShape = TrackTools.getTrackDirectionRaw(state);
                double h = 0.0D;
                if (trackShape.isAscending())
                    h = 0.5D;

                EntityMinecart cart = cartType.makeCart(cartStack, world, pos.getX() + 0.5, pos.getY() + 0.0625D + h, pos.getZ() + 0.5);
                if (cartStack.hasDisplayName())
                    cart.setCustomNameTag(cartStack.getDisplayName());
                CartToolsAPI.setCartOwner(cart, owner);
                world.spawnEntityInWorld(cart);
                return cart;
            }
        return null;
    }

    /**
     * Will return true if the cart matches the provided filter item.
     *
     * @param stack the Filter
     * @param cart  the Cart
     * @return true if the item matches the cart
     * @see IMinecart
     */
    public static boolean doesCartMatchFilter(@Nullable ItemStack stack, @Nullable EntityMinecart cart) {
        if (InvTools.isEmpty(stack))
            return false;
        if (cart == null)
            return false;
        if (cart instanceof IMinecart) {
            if (stack.hasDisplayName())
                return ((IMinecart) cart).doesCartMatchFilter(stack, cart) && stack.getDisplayName().equals(cart.getCartItem().getDisplayName());
            return ((IMinecart) cart).doesCartMatchFilter(stack, cart);
        }
        ItemStack cartItem = cart.getCartItem();
        return !InvTools.isEmpty(stack) && InvTools.isCartItemEqual(stack, cartItem, true);
    }

    /**
     * Matches carts from a filter inventory.
     *
     * @param composite The filter inventory
     * @return The cart checker
     */
    public static Predicate<EntityMinecart> matchesCartsIfFiltered(IInventoryComposite composite) {
        return (cart) -> composite.stackStream().anyMatch((stack) -> doesCartMatchFilter(stack, cart));
    }

    public static void explodeCart(EntityMinecart cart) {
        if (cart.isDead)
            return;
        setTravellingHighSpeed(cart, false);
        cart.motionX = 0;
        cart.motionZ = 0;
        if (Game.isClient(cart.worldObj))
            return;
        removePassengers(cart, cart.getPositionVector().addVector(0.0, 1.5, 0.0));
        cart.worldObj.newExplosion(cart, cart.posX, cart.posY, cart.posZ, 3F, true, true);
        if (MiscTools.RANDOM.nextInt(2) == 0)
            cart.setDead();
    }

    public static void setTravellingHighSpeed(EntityMinecart cart, boolean flag) {
        cart.getEntityData().setBoolean(HIGH_SPEED_TAG, flag);
    }

    public static boolean isTravellingHighSpeed(EntityMinecart cart) {
        return cart.getEntityData().getBoolean(HIGH_SPEED_TAG);
    }

    public static boolean cartVelocityIsLessThan(EntityMinecart cart, float vel) {
        return Math.abs(cart.motionX) < vel && Math.abs(cart.motionZ) < vel;
    }

    public static List<EntityMinecart> getMinecartsIn(World world, AxisAlignedBB searchBox) {
        List<EntityMinecart> entities = world.getEntitiesWithinAABB(EntityMinecart.class, searchBox);
        return entities.stream().filter(cart -> !cart.isDead).collect(Collectors.toList());
    }

    public static List<UUID> getMinecartUUIDsAt(World world, BlockPos pos, float sensitivity) {
        return getMinecartUUIDsAt(world, pos.getX(), pos.getY(), pos.getZ(), sensitivity);
    }

    public static List<UUID> getMinecartUUIDsAt(World world, int i, int j, int k, float sensitivity) {
        sensitivity = Math.min(sensitivity, 0.49f);
        List<EntityMinecart> entities = world.getEntitiesWithinAABB(EntityMinecart.class, new AxisAlignedBB(i + sensitivity, j + sensitivity, k + sensitivity, i + 1 - sensitivity, j + 1 - sensitivity, k + 1 - sensitivity));
        return entities.stream().filter(cart -> !cart.isDead).map(Entity::getPersistentID).collect(Collectors.toList());
    }

    public static void addPassenger(EntityMinecart cart, Entity passenger) {
        passenger.startRiding(cart);
    }

    public static void removePassengers(EntityMinecart cart) {
        removePassengers(cart, cart.getPositionVector());
    }

    public static void removePassengers(EntityMinecart cart, Vec3d resultingPosition) {
        List<Entity> passengers = cart.getPassengers();
        cart.removePassengers();
        for (Entity entity : passengers) {
            if (entity instanceof EntityPlayerMP) {
                EntityPlayerMP player = ((EntityPlayerMP) entity);
                player.setPositionAndUpdate(resultingPosition.xCoord, resultingPosition.yCoord, resultingPosition.zCoord);
            } else
                entity.setLocationAndAngles(resultingPosition.xCoord, resultingPosition.yCoord, resultingPosition.zCoord, entity.rotationYaw, entity.rotationPitch);
        }
    }

    public static EntityPlayer getCartOwnerEntity(EntityMinecart cart) {
        GameProfile owner = CartToolsAPI.getCartOwner(cart);
        EntityPlayer player = null;
        if (!RailcraftConstantsAPI.UNKNOWN_PLAYER.equals(owner.getName()))
            player = PlayerPlugin.getPlayer(cart.worldObj, owner);
        if (player == null)
            player = RailcraftFakePlayer.get((WorldServer) cart.worldObj, cart.posX, cart.posY, cart.posZ);
        return player;
    }

    /**
     * Checks if the entity is in range to render.
     */
    public static boolean isInRangeToRenderDist(EntityMinecart entity, double distance) {
        double range = entity.getEntityBoundingBox().getAverageEdgeLength();

        if (Double.isNaN(range)) {
            range = 1.0D;
        }

        range = range * 64.0D * CartConstants.RENDER_DIST_MULTIPLIER;
        return distance < range * range;
    }

    public static List<String> getDebugOutput(EntityMinecart cart) {
        List<String> debug = new ArrayList<>();
        debug.add("Railcraft Minecart Data Dump");
        String cartInfo;
        if (cart.getEntityWorld().getGameRules().getBoolean("reducedDebugInfo")) {
            cartInfo = String.format("%s[\'%s\'/%d, l=\'%s\']", cart.getClass().getSimpleName(), cart.getName(), cart.getEntityId(), cart.worldObj.getWorldInfo().getWorldName());
        } else {
            cartInfo = cart.toString();
        }
        debug.add("Object: " + cartInfo);
        debug.add("UUID: " + cart.getPersistentID());
        debug.add("Owner: " + CartToolsAPI.getCartOwner(cart).getName());
        LinkageManager lm = LinkageManager.instance();
        debug.add("LinkA: " + lm.getLinkA(cart));
        debug.add("LinkB: " + lm.getLinkB(cart));
        debug.add("Train: " + Train.getTrain(cart).getUUID());
        debug.add("Train Carts:");
        for (UUID uuid : Train.getTrain(cart).getUUIDs()) {
            debug.add("  " + uuid);
        }
        return debug;
    }

    /**
     * Returns a minecart from a persistent UUID. Only returns carts from the same world.
     *
     * @param id Cart's persistent UUID
     * @return EntityMinecart
     */
    @Nullable
    public static EntityMinecart getCartFromUUID(@Nullable World world, @Nullable UUID id) {
        if (world == null) {
            Game.log(Level.ERROR, "Getting cart without a world!", new Throwable("Stacktrace"));
            return null;
        }
        if (id == null) {
            return null;
        }

        if (world instanceof WorldServer) {
            Entity entity = ((WorldServer) world).getEntityFromUuid(id);
            if (entity instanceof EntityMinecart && entity.isEntityAlive()) {
                return (EntityMinecart) entity;
            }
        } else {
            for (Entity entity : world.loadedEntityList) {
                if (entity instanceof EntityMinecart && entity.isEntityAlive() && entity.getPersistentID().equals(id))
                    return (EntityMinecart) entity;
            }
        }
        return null;
    }

    public static boolean startBoost(EntityMinecart cart, BlockPos pos, BlockRailBase.EnumRailDirection dir, double startBoost) {
        World world = cart.worldObj;
        if (dir == BlockRailBase.EnumRailDirection.EAST_WEST) {
            if (world.isSideSolid(pos.west(), EnumFacing.EAST)) {
                cart.motionX = startBoost;
                return true;
            } else if (world.isSideSolid(pos.east(), EnumFacing.WEST)) {
                cart.motionX = -startBoost;
                return true;
            }
        } else if (dir == BlockRailBase.EnumRailDirection.NORTH_SOUTH) {
            if (world.isSideSolid(pos.north(), EnumFacing.SOUTH)) {
                cart.motionZ = startBoost;
                return true;
            } else if (world.isSideSolid(pos.south(), EnumFacing.NORTH)) {
                cart.motionZ = -startBoost;
                return true;
            }
        }
        return false;
    }

    public static void smackCart(EntityMinecart cart, EntityPlayer smacker, float smackVelocity) {
        smackCart(cart, cart, smacker, smackVelocity);
    }

    public static void smackCart(EntityMinecart respect, EntityMinecart cart, EntityPlayer smacker, float smackVelocity) {
        cart.motionX += Math.copySign(smackVelocity, respect.posX - smacker.posX);
        cart.motionZ += Math.copySign(smackVelocity, respect.posZ - smacker.posZ);
    }

}
