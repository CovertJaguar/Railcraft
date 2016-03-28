/* 
 * Copyright (c) CovertJaguar, 2014 http://railcraft.info
 * 
 * This code is the property of CovertJaguar
 * and may only be used with explicit written
 * permission unless otherwise specified on the
 * license page at http://railcraft.info/wiki/info:license.
 */
package mods.railcraft.common.blocks.signals;

import mods.railcraft.api.signals.SignalAspect;
import mods.railcraft.common.plugins.forge.PowerPlugin;
import mods.railcraft.common.plugins.forge.WorldPlugin;
import mods.railcraft.common.util.misc.MiscTools;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static mods.railcraft.common.plugins.forge.PowerPlugin.FULL_POWER;
import static mods.railcraft.common.plugins.forge.PowerPlugin.NO_POWER;

public class TileBoxSequencer extends TileBoxBase {

    private static final int MAX_ITERATIONS = 64;
    private EnumFacing sideOutput = EnumFacing.NORTH;
    private boolean powerState = false;
    private boolean neighborState = false;

    public TileBoxSequencer() {
    }

    @Override
    public EnumSignal getSignalType() {
        return EnumSignal.BOX_SEQUENCER;
    }

    @Override
    public void onNeighborBlockChange(IBlockState state, Block block) {
        super.onNeighborBlockChange(state, block);
        if (worldObj.isRemote)
            return;
        boolean p = PowerPlugin.isBlockBeingPoweredByRepeater(worldObj, getPos());
        if (!powerState && p) {
            powerState = p;
            incrementSequencer(true, new HashSet<TileEntity>(), 0);
        } else
            powerState = p;
    }

    @Override
    public void onNeighborStateChange(TileBoxBase neighbor, EnumFacing side) {
        if (worldObj.isRemote)
            return;
        if (neighbor instanceof TileBoxSequencer)
            return;
        if (neighbor instanceof TileBoxCapacitor)
            return;
        boolean p = neighbor.isEmittingRedstone(side);
        if (!neighborState && p) {
            neighborState = p;
            incrementSequencer(true, new HashSet<TileEntity>(), 0);
        } else
            neighborState = p;
    }

    private void incrementSequencer(boolean firstPass, Set<TileEntity> visitedFirstPass, int numberOfIterations) {
        if (firstPass) {
            visitedFirstPass.add(this);
            TileEntity tile = tileCache.getTileOnSide(sideOutput);
            if (tile instanceof TileBoxSequencer && !visitedFirstPass.contains(tile)) {
                TileBoxSequencer box = (TileBoxSequencer) tile;
                box.incrementSequencer(true, visitedFirstPass, numberOfIterations);
                return;
            }
        }

        EnumFacing newSide = EnumFacing.getFront((sideOutput.ordinal() + 1) % 6);;
        while (newSide != sideOutput && !canOutputToSide(newSide)) {
            newSide = EnumFacing.getFront((newSide.ordinal() +1) % 6);
        }
        sideOutput = newSide;
        updateNeighbors();

        if (numberOfIterations >= MAX_ITERATIONS)
            return;

        TileEntity tile = tileCache.getTileOnSide(sideOutput);
        if (tile instanceof TileBoxSequencer) {
            TileBoxSequencer box = (TileBoxSequencer) tile;
            box.incrementSequencer(false, visitedFirstPass, numberOfIterations + 1);
        }
    }

    private boolean canOutputToSide(EnumFacing side) {
        TileEntity tile = tileCache.getTileOnSide(side);
        if (tile instanceof TileBoxSequencer)
            return true;
        if (tile instanceof TileBoxBase)
            return ((TileBoxBase) tile).canReceiveAspect();
        IBlockState state = WorldPlugin.getBlockState(worldObj, getPos().offset(side));
        Block block = state.getBlock();
        if (block == Blocks.redstone_wire)
            return true;
        if (block == Blocks.unpowered_repeater || block == Blocks.powered_repeater) {
            EnumFacing facing = state.getValue(BlockDirectional.FACING);
            return side == facing;
        }
        return false;
    }

    private void updateNeighbors() {
        sendUpdateToClient();
        notifyBlocksOfNeighborChange();
        updateNeighborBoxes();
    }

    @Override
    public int getPowerOutput(EnumFacing side) {
        TileEntity tile = tileCache.getTileOnSide(side.getOpposite());
        if (tile instanceof TileBoxBase)
            return NO_POWER;
        return sideOutput.getOpposite() == side ? FULL_POWER : NO_POWER;
    }

    @Override
    public boolean isEmittingRedstone(EnumFacing side) {
        return sideOutput == side;
    }

    @Override
    public SignalAspect getBoxSignalAspect(EnumFacing side) {
        return sideOutput == side ? SignalAspect.GREEN : SignalAspect.RED;
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setByte("sideOutput", (byte) sideOutput.ordinal());
        data.setBoolean("powerState", powerState);
        data.setBoolean("neighborState", neighborState);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        sideOutput = EnumFacing.getFront(data.getByte("sideOutput"));
        powerState = data.getBoolean("powerState");
        neighborState = data.getBoolean("neighborState");
    }

    @Override
    public void writePacketData(DataOutputStream data) throws IOException {
        super.writePacketData(data);
        data.writeByte(sideOutput.ordinal());
    }

    @Override
    public void readPacketData(DataInputStream data) throws IOException {
        super.readPacketData(data);
        sideOutput = EnumFacing.getFront(data.readByte());
        markBlockForUpdate();
    }

    @Override
    public boolean isConnected(EnumFacing side) {
        TileEntity tile = tileCache.getTileOnSide(side);
        if (tile instanceof TileBoxSequencer)
            return true;
        if (tile instanceof TileBoxBase)
            return ((TileBoxBase) tile).canReceiveAspect() || ((TileBoxBase) tile).canTransferAspect();
        return false;
    }

    @Override
    public boolean canTransferAspect() {
        return true;
    }

    @Override
    public boolean canReceiveAspect() {
        return true;
    }
}
