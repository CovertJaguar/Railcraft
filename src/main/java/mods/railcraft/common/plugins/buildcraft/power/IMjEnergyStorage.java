/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2021
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/

package mods.railcraft.common.plugins.buildcraft.power;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

public interface IMjEnergyStorage extends INBTSerializable<NBTTagCompound> {
    long getStored();

    long getCapacity();

    long addPower(long microJoulesToAdd, boolean simulate);

    boolean extractPower(long power);

    long extractPower(long min, long max);
}
