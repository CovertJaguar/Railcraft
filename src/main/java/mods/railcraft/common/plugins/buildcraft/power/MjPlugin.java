/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2021
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/

package mods.railcraft.common.plugins.buildcraft.power;

import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjAPI;
import mods.railcraft.common.util.misc.Capabilities;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional.Method;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;

public class MjPlugin {
    public static final DecimalFormat FORMAT = new DecimalFormat("0.0");
    public static final long MJ = 1_000_000L;
    public static boolean LOADED;

    @CapabilityInject(IMjConnector.class)
    @javax.annotation.Nullable
    public static Capability<IMjConnector> CONNECTOR_CAPABILITY;

    public static final IMjEnergyStorage DUMMY_STORAGE = new IMjEnergyStorage() {
        @Override
        public long getStored() {
            return 0;
        }

        @Override
        public long getCapacity() {
            return 0;
        }

        @Override
        public long addPower(long microJoulesToAdd, boolean simulate) {
            return 0;
        }

        @Override
        public boolean extractPower(long power) {
            return false;
        }

        @Override
        public long extractPower(long min, long max) {
            return 0;
        }

        @Override
        public NBTTagCompound serializeNBT() {
            return new NBTTagCompound();
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbtTagCompound) {}
    };

    public static IMjEnergyStorage getMjEnergyStorage(long capacity, long maxReceive, long maxExtract) {
        if (LOADED)
            return _getMjEnergyStorage(capacity, maxReceive, maxExtract);

        return MjPlugin.DUMMY_STORAGE;
    }

    @Method(modid = "buildcraftlib")
    public static IMjEnergyStorage _getMjEnergyStorage(long capacity, long maxReceive, long maxExtract) {
        return new MjEnergyStorage(capacity, maxReceive, maxExtract);
    }

    @SuppressWarnings("UnusedReturnValue")
    public static long pushToTile(TileEntity tile, EnumFacing side, long powerToTransfer) {
        return LOADED ? _pushToTile(tile, side, powerToTransfer) : 0;
    }

    @Method(modid = "buildcraftlib")
    public static long _pushToTile(TileEntity tile, EnumFacing side, long powerToTransfer) {
        if (canTileReceivePower(tile, side)) {
            IMjReceiver receiver = tile.getCapability(MjAPI.CAP_RECEIVER, side);
            if (receiver != null && powerToTransfer > 0)
                return receiver.receivePower(Math.min(powerToTransfer, receiver.getPowerRequested()), false);
        }
        return 0;
    }

    public static boolean canTileReceivePower(@Nullable TileEntity tile, EnumFacing side) {
        return LOADED ? _canTileReceivePower(tile, side) : false;
    }

    @Method(modid = "buildcraftlib")
    public static boolean _canTileReceivePower(@Nullable TileEntity tile, EnumFacing side) {
        return Capabilities.get(tile, MjAPI.CAP_RECEIVER, side).map(IMjReceiver::canReceive).orElse(false);
    }
}
