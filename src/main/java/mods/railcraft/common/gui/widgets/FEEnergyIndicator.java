/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2019
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.gui.widgets;

import net.minecraftforge.energy.IEnergyStorage;

/**
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public class FEEnergyIndicator extends IndicatorController {

    protected final IEnergyStorage energyStorage;
    protected int energy;

    public FEEnergyIndicator(IEnergyStorage energyStorage) {
        this.energyStorage = energyStorage;
    }

    @Override
    protected void refreshToolTip() {
        tip.text = String.format("%,d / %,d FE", energy, energyStorage.getMaxEnergyStored());
    }

    @Override
    public double getMeasurement() {
        double e = Math.min(energy, energyStorage.getMaxEnergyStored());
        return e / energyStorage.getMaxEnergyStored();
    }

    @Override
    public void setClientValue(double value) {
        energy = (int) value;
    }

    @Override
    public double getClientValue() {
        return energy;
    }

    @Override
    public double getServerValue() {
        return energyStorage.getEnergyStored();
    }
}
