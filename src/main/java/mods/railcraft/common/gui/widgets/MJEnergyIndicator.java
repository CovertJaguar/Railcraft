/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2021
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/

package mods.railcraft.common.gui.widgets;

import mods.railcraft.common.plugins.buildcraft.power.IMjEnergyStorage;
import mods.railcraft.common.plugins.buildcraft.power.MjPlugin;

public class MJEnergyIndicator extends IndicatorController {

    protected final IMjEnergyStorage energyStorage;
    protected long energy;

    public MJEnergyIndicator(IMjEnergyStorage energyStorage) {
        this.energyStorage = energyStorage;
    }

    @Override
    public double getMeasurement() {
        double e = Math.min(energy, energyStorage.getCapacity());
        return e / energyStorage.getCapacity();
    }

    @Override
    public void setClientValue(double value) {
        energy = Math.round(value);
    }

    @Override
    public double getClientValue() {
        return energy;
    }

    @Override
    public double getServerValue() {
        return energyStorage.getStored();
    }

    @Override
    protected void refreshToolTip() {
        tip.text = String.format("%,d / %,d MJ", energy / MjPlugin.MJ, energyStorage.getCapacity() / MjPlugin.MJ);
    }
}
