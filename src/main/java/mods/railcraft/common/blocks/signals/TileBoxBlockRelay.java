/* 
 * Copyright (c) CovertJaguar, 2014 http://railcraft.info
 * 
 * This code is the property of CovertJaguar
 * and may only be used with explicit written
 * permission unless otherwise specified on the
 * license page at http://railcraft.info/wiki/info:license.
 */
package mods.railcraft.common.blocks.signals;

import mods.railcraft.api.signals.*;
import mods.railcraft.common.gui.EnumGui;
import mods.railcraft.common.gui.GuiHandler;
import mods.railcraft.common.plugins.buildcraft.triggers.IAspectProvider;
import mods.railcraft.common.plugins.forge.WorldPlugin;
import mods.railcraft.common.util.misc.Game;
import mods.railcraft.common.util.network.IGuiReturnHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static mods.railcraft.common.plugins.forge.PowerPlugin.FULL_POWER;
import static mods.railcraft.common.plugins.forge.PowerPlugin.NO_POWER;

public class TileBoxBlockRelay extends TileBoxActionManager implements ISignalBlockTile, IAspectActionManager, IGuiReturnHandler, IAspectProvider {

    private final SimpleSignalController controller = new SimpleSignalController(getLocalizationTag(), this);
    private final SignalBlock signalBlock = new SignalBlockRelay(getLocalizationTag(), this);

    @Override
    public EnumSignal getSignalType() {
        return EnumSignal.BOX_BLOCK_RELAY;
    }

    @Override
    public boolean blockActivated(EnumFacing side, EntityPlayer player) {
        if (player.isSneaking())
            return false;
        if (Game.isHost(worldObj))
            GuiHandler.openGui(EnumGui.BOX_RELAY, player, worldObj, getPos().getX(), getPos().getY(), getPos().getZ());
        return true;
    }

    @Override
    public boolean canUpdate() {
        return true;
    }

    @Override
    public void update() {
        super.update();
        if (Game.isNotHost(worldObj)) {
            controller.tickClient();
            signalBlock.tickClient();
            return;
        }
        controller.tickServer();
        signalBlock.tickServer();
        SignalAspect prevAspect = controller.getAspect();
        if (controller.isBeingPaired())
            controller.setAspect(SignalAspect.BLINK_YELLOW);
        else
            controller.setAspect(signalBlock.getSignalAspect());
        if (prevAspect != controller.getAspect()) {
            updateNeighbors();
            sendUpdateToClient();
        }
    }

    private void updateNeighbors() {
        notifyBlocksOfNeighborChange();
        for (EnumFacing side : EnumFacing.HORIZONTALS) {
            TileEntity tile = tileCache.getTileOnSide(side);
            if (tile instanceof TileBoxBase) {
                TileBoxBase box = (TileBoxBase) tile;
                box.onNeighborStateChange(this, side);
            }
        }
    }

    @Override
    public int getPowerOutput(EnumFacing side) {
        TileEntity tile = WorldPlugin.getTileEntityOnSide(worldObj, getPos(), side.getOpposite());
        if (tile instanceof TileBoxBase)
            return NO_POWER;
        return isEmittingRedstone(side) ? FULL_POWER : NO_POWER;
    }

    @Override
    public boolean isEmittingRedstone(EnumFacing side) {
        return doesActionOnAspect(getBoxSignalAspect(side));
    }

    @Override
    public boolean canEmitRedstone(EnumFacing side) {
        return true;
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);

        signalBlock.writeToNBT(data);
        controller.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);

        signalBlock.readFromNBT(data);
        controller.readFromNBT(data);
    }

    @Override
    public void writePacketData(DataOutputStream data) throws IOException {
        super.writePacketData(data);
        controller.writePacketData(data);
        signalBlock.writePacketData(data);
    }

    @Override
    public void readPacketData(DataInputStream data) throws IOException {
        super.readPacketData(data);
        controller.readPacketData(data);
        signalBlock.readPacketData(data);
        markBlockForUpdate();
    }

    @Override
    public void readGuiData(DataInputStream data, EntityPlayer sender) throws IOException {
        super.readGuiData(data, sender);
        updateNeighbors();
    }

//    @Override
//    public SimpleSignalController getController() {
//        return controller;
//    }

    @Override
    public SignalBlock getSignalBlock() {
        return signalBlock;
    }

    @Override
    public void doActionOnAspect(SignalAspect aspect, boolean trigger) {
        super.doActionOnAspect(aspect, trigger);
        updateNeighbors();
    }

    @Override
    public boolean isConnected(EnumFacing side) {
        TileEntity tile = tileCache.getTileOnSide(side);
        if (tile instanceof TileBoxBase)
            return ((TileBoxBase) tile).canReceiveAspect();
        return false;
    }

    @Override
    public SignalAspect getBoxSignalAspect(EnumFacing side) {
        return controller.getAspect();
    }

    @Override
    public boolean canTransferAspect() {
        return true;
    }

    @Override
    public SignalAspect getTriggerAspect() {
        return getBoxSignalAspect(null);
    }

}
