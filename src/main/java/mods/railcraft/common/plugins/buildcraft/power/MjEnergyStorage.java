/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2021
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/

package mods.railcraft.common.plugins.buildcraft.power;

import buildcraft.api.mj.MjBattery;

public class MjEnergyStorage extends MjBattery implements IMjEnergyStorage {
    protected long maxReceive;
    protected long maxExtract;

    public MjEnergyStorage(long capacity, long maxReceive, long maxExtract) {
        super(capacity);
        this.maxReceive = maxReceive;
        this.maxExtract = maxExtract;
    }

    @Override
    public long addPower(long microJoulesToAdd, boolean simulate) {
        long energyReceived = Math.min(getCapacity() - getStored(), Math.min(maxReceive, microJoulesToAdd));
        return super.addPower(energyReceived, simulate);
    }
}
