/*
 * Copyright (c) CovertJaguar, 2011-2017
 * http://railcraft.info
 *
 * This code is the property of CovertJaguar
 * and may only be used with explicit written
 * permission unless otherwise specified on the
 * license page at http://railcraft.info/wiki/info:license.
 */
package mods.railcraft.common.blocks.machine.manipulator;

import mods.railcraft.common.blocks.machine.TileMachineItem;
import mods.railcraft.common.blocks.machine.interfaces.ITileRotate;
import mods.railcraft.common.util.misc.MiscTools;
import mods.railcraft.common.util.network.RailcraftInputStream;
import mods.railcraft.common.util.network.RailcraftOutputStream;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nullable;
import java.io.IOException;

/**
 * Created by CovertJaguar on 9/12/2016 for Railcraft.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public abstract class TileManipulator extends TileMachineItem implements ITileRotate {
    protected EnumFacing facing = getDefaultFacing();

    protected TileManipulator() {
    }

    protected TileManipulator(int invSize) {
        super(invSize);
    }

    @Override
    public void onBlockPlacedBy(IBlockState state, @Nullable EntityLivingBase entityLiving, ItemStack stack) {
        super.onBlockPlacedBy(state, entityLiving, stack);
        if (canRotate()) {
            facing = MiscTools.getSideFacingTrack(worldObj, getPos());
            if (facing == null)
                if (entityLiving != null) {
                    facing = MiscTools.getSideFacingPlayer(getPos(), entityLiving);
                } else facing = getDefaultFacing();
        }
    }

    public EnumFacing getDefaultFacing() {
        EnumFacing[] validRotation = getValidRotations();
        if (validRotation != null)
            return validRotation[0];
        return EnumFacing.DOWN;
    }

    @Override
    public EnumFacing getFacing() {
        return facing;
    }

    @Override
    public void setFacing(EnumFacing facing) {
        this.facing = facing;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        if (canRotate())
            data.setByte("direction", (byte) facing.ordinal());
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        if (canRotate())
            facing = EnumFacing.getFront(data.getByte("direction"));
    }

    @Override
    public void writePacketData(RailcraftOutputStream data) throws IOException {
        super.writePacketData(data);
        if (canRotate())
            data.writeByte(facing.ordinal());
    }

    @Override
    public void readPacketData(RailcraftInputStream data) throws IOException {
        super.readPacketData(data);
        if (canRotate())
            facing = EnumFacing.getFront(data.readByte());
    }

}
