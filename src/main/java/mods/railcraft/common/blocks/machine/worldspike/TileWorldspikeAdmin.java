/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2020
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.blocks.machine.worldspike;

/**
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class TileWorldspikeAdmin extends TileWorldspike {

    @Override
    public WorldspikeVariant getMachineType() {
        return WorldspikeVariant.ADMIN;
    }

    @Override
    public boolean usesFuel() {
        return false;
    }
}
