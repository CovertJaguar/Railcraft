/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2019
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.blocks.single;

import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjReceiver;
import mods.railcraft.common.blocks.ISmartTile;
import mods.railcraft.common.blocks.TileRailcraftTicking;
import mods.railcraft.common.blocks.interfaces.ITileNonSolid;
import mods.railcraft.common.blocks.interfaces.ITileRotate;
import mods.railcraft.common.gui.widgets.MJEnergyIndicator;
import mods.railcraft.common.plugins.buildcraft.power.IMjEnergyStorage;
import mods.railcraft.common.plugins.buildcraft.power.MjPlugin;
import mods.railcraft.common.plugins.forge.PowerPlugin;
import mods.railcraft.common.util.misc.Game;
import mods.railcraft.common.util.network.RailcraftInputStream;
import mods.railcraft.common.util.network.RailcraftOutputStream;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * @author CovertJaguar <http://www.railcraft.info>
 */
public abstract class TileEngine extends TileRailcraftTicking implements ITileRotate, ITileNonSolid, ISmartTile {
    public double currentOutput;
    private float pistonProgress;
    private int pistonStage;
    private boolean powered;
    private boolean isActive;
    private boolean needsInit = true;
    //    public int outputDebug, genDebug, cycleTick;
    private EnergyStage energyStage = EnergyStage.BLUE;
    protected final MjConnector connector = new MjConnector();
    protected final IMjEnergyStorage storage = MjPlugin.getMjEnergyStorage(maxEnergy(), maxEnergyReceived(), maxEnergyExtracted());
    public final MJEnergyIndicator mjIndicator = new MJEnergyIndicator(storage);

    protected TileEngine() {}

    protected void playSoundIn() {}

    protected void playSoundOut() {}

    @Override
    public void update() {
        if (!MjPlugin.LOADED)
            return;

        super.update();
        if (Game.isClient(world)) {
            if (pistonStage != 0) {
                pistonProgress += getPistonSpeed();
                if (pistonProgress > 0.5 && pistonStage == 1) {
                    pistonStage = 2;
                    playSoundOut();
                } else if (pistonProgress >= 1) {
                    pistonStage = 0;
                    pistonProgress = 0;
                    playSoundIn();
                }
            } else if (isActive)
                pistonStage = 1;

            return;
        }

        if (needsInit) {
            needsInit = false;
            checkPower();
        }

        if (!powered)
            if (storage.getStored() > 1)
                storage.extractPower(MjPlugin.MJ);

        EnumFacing direction = getFacing();
        if (getEnergyStage() == EnergyStage.OVERHEAT)
            overheat();
        else if (pistonStage != 0) {
            pistonProgress += getPistonSpeed();

            if (pistonProgress > 0.5 && pistonStage == 1) {
                pistonStage = 2;
                TileEntity tile = tileCache.getTileOnSide(direction);

                if (MjPlugin.canTileReceivePower(tile, direction.getOpposite())) {
                    MjPlugin.pushToTile(tile, direction.getOpposite(), extractEnergy());
                }
            } else if (pistonProgress >= 1) {
                pistonProgress = 0;
                pistonStage = 0;
//                ChatPlugin.sendLocalizedChatToAllFromServer(world, "Ticks=%d, Gen=%d, Out=%d", clock - cycleTick, genDebug, outputDebug);
//                outputDebug = 0;
//                genDebug = 0;
//                cycleTick = clock;
            }
        } else if (powered) {
            TileEntity tile = tileCache.getTileOnSide(direction);

            if (MjPlugin.canTileReceivePower(tile, direction.getOpposite()))
                if (storage.getStored() > 0) {
                    pistonStage = 1;
                    setActive(true);
                } else
                    setActive(false);
            else
                setActive(false);
        } else
            setActive(false);

        burn();
    }

    protected void overheat() {
        subtractEnergy(5 * MjPlugin.MJ);
    }

    protected abstract void burn();

    public boolean isActive() {
        return isActive;
    }

    private void setActive(boolean isActive) {
        if (this.isActive != isActive) {
            this.isActive = isActive;
            sendUpdateToClient();
        }
    }

    public double getPistonSpeed() {
        if (Game.isHost(world))
            return Math.max(0.16 * getEnergyLevel(), 0.01);
        switch (getEnergyStage()) {
            case BLUE:
                return 0.01;
            case GREEN:
                return 0.02;
            case YELLOW:
                return 0.04;
            case ORANGE:
                return 0.08;
            case RED:
                return 0.16;
            default:
                return 0.0;
        }
    }

    @Override
    public boolean rotateBlock(EnumFacing axis) {
        if (getEnergyStage() == EnergyStage.OVERHEAT) {
            resetEnergyStage();
            return true;
        }
        return switchOrientation();
    }

    @Override
    public void onBlockPlacedBy(IBlockState state, @Nullable EntityLivingBase entityLiving, ItemStack stack) {
        super.onBlockPlacedBy(state, entityLiving, stack);
        switchOrientation();
        checkPower();
    }

    @Override
    public void onNeighborBlockChange(IBlockState state, Block block, BlockPos pos) {
        super.onNeighborBlockChange(state, block, pos);
        if (Game.isClient(getWorld()))
            return;
        checkPower();
    }

    private void checkPower() {
        boolean p = PowerPlugin.isBlockBeingPowered(world, getPos());
        if (powered != p) {
            powered = p;
            sendUpdateToClient();
        }
    }

    public boolean isPowered() {
        return powered;
    }

    @Override
    public boolean canConnectRedstone(@Nullable EnumFacing dir) {
        return true;
    }

    public boolean switchOrientation() {
        EnumFacing direction = getFacing();
        for (int i = direction.ordinal() + 1; i < direction.ordinal() + 6; ++i) {
            EnumFacing dir = EnumFacing.byIndex(i % 6);

            TileEntity tile = tileCache.getTileOnSide(dir);

            if (MjPlugin.canTileReceivePower(tile, dir.getOpposite())) {
                setFacing(dir);
                notifyBlocksOfNeighborChange();
                sendUpdateToClient();
                return true;
            }
        }
        return false;
    }

    @Override
    public BlockFaceShape getShape(EnumFacing side) {
        return getFacing().getOpposite() == side ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
    }

    public double getEnergyLevel() {
        return storage.getStored() / (double) maxEnergy();
    }

    protected EnergyStage computeEnergyStage() {
        double energyLevel = getEnergyLevel();
        if (energyLevel < 0.2)
            return EnergyStage.BLUE;
        else if (energyLevel < 0.4)
            return EnergyStage.GREEN;
        else if (energyLevel < 0.6)
            return EnergyStage.YELLOW;
        else if (energyLevel < 0.8)
            return EnergyStage.ORANGE;
        else if (energyLevel < 1)
            return EnergyStage.RED;
        else
            return EnergyStage.OVERHEAT;
    }

    public final EnergyStage getEnergyStage() {
        if (hasWorld() && Game.isHost(world)) {
            if (energyStage == EnergyStage.OVERHEAT)
                return energyStage;
            EnergyStage newStage = computeEnergyStage();

            if (energyStage != newStage) {
                energyStage = newStage;
                sendUpdateToClient();
            }
        }

        return energyStage;
    }

    public final void resetEnergyStage() {
        EnergyStage newStage = computeEnergyStage();

        if (energyStage != newStage) {
            energyStage = newStage;
            sendUpdateToClient();
        }
    }

    public void addEnergy(long addition) {
        storage.addPower(addition, false);
    }

    public void subtractEnergy(long subtraction) {
        storage.extractPower(subtraction);
    }

    public long extractEnergy() {
        return storage.extractPower(maxEnergyExtracted(), maxEnergyExtracted());
    }

    public float getProgress() {
        return hasWorld() ? pistonProgress : 0.25F;
    }

    public abstract long maxEnergy();

    public abstract long maxEnergyExtracted();

    public abstract long maxEnergyReceived();

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);

        data.setBoolean("powered", powered);
        data.setTag("energy", storage.serializeNBT());
        data.setDouble("currentOutput", currentOutput);
        data.setByte("energyStage", (byte) energyStage.ordinal());
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);

        powered = data.getBoolean("powered");
        storage.deserializeNBT(data.getCompoundTag("energy"));
        currentOutput = data.getDouble("currentOutput");
        energyStage = EnergyStage.fromOrdinal(data.getByte("energyStage"));
    }

    @Override
    public void writePacketData(RailcraftOutputStream data) throws IOException {
        super.writePacketData(data);

        data.writeByte(getEnergyStage().ordinal());
        data.writeBoolean(isActive);
    }

    @Override
    public void readPacketData(RailcraftInputStream data) throws IOException {
        super.readPacketData(data);

        energyStage = EnergyStage.fromOrdinal(data.readByte());
        isActive = data.readBoolean();
    }

    @Override
    public EnumFacing getFacing() {
        if (hasWorld()) {
            IBlockState state = getBlockState();
            if (state.getProperties().containsKey(BlockEngine.FACING)) return state.getValue(BlockEngine.FACING);
        }
        return EnumFacing.UP;
    }

    @Override
    public void setFacing(EnumFacing facing) {
        if (hasWorld())
            world.setBlockState(pos, getBlockState().withProperty(BlockEngine.FACING, facing));
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return facing == getFacing() && MjPlugin.CONNECTOR_CAPABILITY != null|| super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (facing == getFacing() && capability == MjPlugin.CONNECTOR_CAPABILITY) return MjPlugin.CONNECTOR_CAPABILITY.cast(connector);
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState != newState;
    }

    public enum EnergyStage {

        BLUE, GREEN, YELLOW, ORANGE, RED, OVERHEAT;
        public static final EnergyStage[] VALUES = values();

        public static EnergyStage fromOrdinal(int ordinal) {
            if (ordinal < 0 || ordinal >= VALUES.length)
                return BLUE;
            return VALUES[ordinal];
        }

    }

    @Optional.Interface(iface = "buildcraft.api.mj.IMjConnector", modid = "buildcraftlib")
    private static class MjConnector implements IMjConnector {
        @Override
        public boolean canConnect(@NotNull IMjConnector other) {
            return other instanceof IMjReceiver && ((IMjReceiver) other).canReceive();
        }
    }
}
