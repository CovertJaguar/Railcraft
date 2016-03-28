/* 
 * Copyright (c) CovertJaguar, 2014 http://railcraft.info
 * 
 * This code is the property of CovertJaguar
 * and may only be used with explicit written
 * permission unless otherwise specified on the
 * license page at http://railcraft.info/wiki/info:license.
 */
package mods.railcraft.common.blocks.detector;

import mods.railcraft.api.carts.CartTools;
import mods.railcraft.common.blocks.RailcraftTileEntity;
import mods.railcraft.common.plugins.forge.PowerPlugin;
import mods.railcraft.common.plugins.forge.WorldPlugin;
import mods.railcraft.common.util.misc.Game;
import mods.railcraft.common.util.misc.SafeNBTWrapper;
import mods.railcraft.common.util.network.IGuiReturnHandler;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class TileDetector extends RailcraftTileEntity implements IGuiReturnHandler {

    public static final float SENSITIVITY = 0.2f;
    private static final int POWER_DELAY = 10;
    public EnumFacing direction = EnumFacing.UP;
    public int powerState = 0;
    public Detector detector = Detector.DUMMY;
    private boolean tested;
    private int powerDelay = 0;

    public Detector getDetector() {
        return detector;
    }

    public void setDetector(EnumDetector type) {
        this.detector = type.buildHandler();
        detector.setTile(this);
        if (worldObj != null) {
            markBlockForUpdate();
            notifyBlocksOfNeighborChange();
        }
    }

    @Override
    public String getLocalizationTag() {
        return getDetector().getType().getTag() + ".name";
    }

    public List<EntityMinecart> getCarts() {
        return CartTools.getMinecartsOnAllSides(worldObj, getPos(), SENSITIVITY);
    }

    public boolean blockActivated(EntityPlayer player) {
        return detector.blockActivated(player);
    }

    public void onNeighborBlockChange(Block block) {
        detector.onNeighborBlockChange(block);
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);

        data.setByte("type", (byte) detector.getType().ordinal());
        detector.writeToNBT(data);
        data.setByte("direction", (byte) direction.ordinal());
        data.setByte("powerState", (byte) powerState);
        data.setByte("powerDelay", (byte) powerDelay);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);

        SafeNBTWrapper safe = new SafeNBTWrapper(data);

        direction = EnumFacing.getFront(safe.getByte("direction"));
        powerState = data.getByte("powerState");
        powerDelay = data.getByte("powerDelay");

        if (data.hasKey("type"))
            setDetector(EnumDetector.fromOrdinal(data.getByte("type")));
        detector.readFromNBT(data);
    }

    @Override
    public void writePacketData(DataOutputStream data) throws IOException {
        super.writePacketData(data);
        data.writeByte(detector.getType().ordinal());
        data.writeByte(powerState);
        data.writeByte(direction.ordinal());
        detector.writePacketData(data);
    }

    @Override
    public void readPacketData(DataInputStream data) throws IOException {
        super.readPacketData(data);
        int type = data.readByte();
        if (detector == Detector.DUMMY || detector.getType().ordinal() != type)
            setDetector(EnumDetector.fromOrdinal(type));
        powerState = data.readByte();
        direction = EnumFacing.getFront(data.readByte());
        detector.readPacketData(data);
        markBlockForUpdate();
    }

    @Override
    public void update() {
        super.update();
        if (Game.isNotHost(getWorld()))
            return;
        if (!tested) {
            tested = true;
            IBlockState state =  worldObj.getBlockState(getPos());
            int meta = state.getBlock().getMetaFromState(state);
            if (meta != 0) {
                worldObj.removeTileEntity(getPos());
                Block block = BlockDetector.getBlock();
                if (block != null)
                    worldObj.setBlockState(getPos(), block.getDefaultState(), 3);
            }
        }
        if (powerDelay > 0)
            powerDelay--;
        else if (detector.updateInterval() == 0 || clock % detector.updateInterval() == 0) {
            int newPowerState = detector.shouldTest() ? detector.testCarts(getCarts()) : PowerPlugin.NO_POWER;
            if (newPowerState != powerState) {
                powerState = newPowerState;
                if (powerState > PowerPlugin.NO_POWER)
                    powerDelay = POWER_DELAY;
                sendUpdateToClient();
                worldObj.notifyNeighborsOfStateChange(getPos(), BlockDetector.getBlock());
                WorldPlugin.notifyBlocksOfNeighborChangeOnSide(worldObj, getPos(), BlockDetector.getBlock(), direction);
            }
        }
    }

    @Override
    public short getId() {
        return 76;
    }

    @Override
    public void writeGuiData(DataOutputStream data) throws IOException {
        detector.writeGuiData(data);
    }

    @Override
    public void readGuiData(DataInputStream data, EntityPlayer sender) throws IOException {
        detector.readGuiData(data, sender);
    }
}
