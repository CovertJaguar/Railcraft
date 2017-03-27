/*
 * Copyright (c) CovertJaguar, 2011-2017
 * http://railcraft.info
 *
 * This code is the property of CovertJaguar
 * and may only be used with explicit written
 * permission unless otherwise specified on the
 * license page at http://railcraft.info/wiki/info:license.
 */
package mods.railcraft.common.carts;

import ic2.api.item.IC2Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public final class EntityCartEnergyMFSU extends CartBaseEnergy {

    public EntityCartEnergyMFSU(World world) {
        super(world);
    }

    public EntityCartEnergyMFSU(World world, double d, double d1, double d2) {
        this(world);
        setPosition(d, d1 + getYOffset(), d2);
        motionX = 0.0D;
        motionY = 0.0D;
        motionZ = 0.0D;
        prevPosX = d;
        prevPosY = d1;
        prevPosZ = d2;
    }

    @Override
    public int getTier() {
        return 3;
    }

    @Override
    public ItemStack getIC2Item() {
        return IC2Items.getItem("mfsUnit");
    }

    @Override
    public int getCapacity() {
        return 10000000;
    }

    @Override
    public int getTransferLimit() {
        return 512;
    }

    @Override
    public IRailcraftCartContainer getCartType() {
        return RailcraftCarts.ENERGY_MFSU;
    }

}
