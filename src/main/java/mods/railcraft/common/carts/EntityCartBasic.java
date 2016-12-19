/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2016
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.carts;

import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityMinecartEmpty;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class EntityCartBasic extends EntityMinecartEmpty implements IRailcraftCart {

    public EntityCartBasic(World world) {
        super(world);
    }

    public EntityCartBasic(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    @Override
    public IRailcraftCartContainer getCartType() {
        return RailcraftCarts.BASIC;
    }

    @Nullable
    @Override
    public ItemStack getCartItem() {
        return createCartItem(this);
    }

    @Override
    public void killMinecart(DamageSource par1DamageSource) {
        killAndDrop(this);
    }

    /**
     * Checks if the entity is in range to render.
     */
    @Override
    @SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double distance) {
        return CartTools.isInRangeToRenderDist(this, distance);
    }
//    @Override
//    protected double getDrag() {
//        if (RailcraftConfig.adjustBasicCartDrag()) {
//            return CartConstants.STANDARD_DRAG;
//        }
//        return super.getDrag();
//    }
//    @Override
//    public void onUpdate() {
//        if (Game.isHost(world) && world instanceof WorldServer) {
//            int blockId = world.getBlockId((int) posX, (int) posY, (int) posZ);
//
//            if (blockId == Block.portal.blockID) {
//                setInPortal();
//            }
//
//            if (inPortal) {
//                MinecraftServer mc = ((WorldServer) world).getMinecraftServer();
//                if (mc.getAllowNether()) {
//                    int maxPortalTime = getMaxInPortalTime();
//                    if (ridingEntity == null && field_82153_h++ >= maxPortalTime) {
//                        field_82153_h = maxPortalTime;
//                        timeUntilPortal = getPortalCooldown();
//                        byte dim;
//
//                        if (world.provider.getDimensionId() == -1) {
//                            dim = 0;
//                        } else {
//                            dim = -1;
//                        }
//
//                        Entity rider = riddenByEntity;
//                        if (rider != null) {
//                            rider.setInPortal();
//                            rider.timeUntilPortal = rider.getPortalCooldown();
//                            rider.travelToDimension(dim);
//                        }
//                        travelToDimension(dim);
//                    }
//
//                    inPortal = false;
//                }
//            }
//        }
//
//        super.onUpdate();
//    }

    @Override
    public void moveMinecartOnRail(BlockPos pos) {
        double mX = motionX;
        double mZ = motionZ;

//        if (this.riddenByEntity != null)
//        {
//            mX *= 0.75D;
//            mZ *= 0.75D;
//        }

        double max = getMaxSpeed();
        mX = MathHelper.clamp(mX, -max, max);
        mZ = MathHelper.clamp(mZ, -max, max);
        move(MoverType.SELF, mX, 0.0D, mZ);
    }
}
