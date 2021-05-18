/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2019
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.blocks.single;

import mods.railcraft.common.gui.EnumGui;
import mods.railcraft.common.plugins.buildcraft.power.MjPlugin;

/**
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class TileEngineSteamLow extends TileEngineSteam {

    private static final long OUTPUT_MJ = 4 * MjPlugin.MJ;
    private static final long CAPACITY = 20000 * MjPlugin.MJ;
    private static final long RECEIVE = 600 * MjPlugin.MJ;
    private static final long EXTRACT = 80 * MjPlugin.MJ;

    @Override
    public EnumGui getGui() {
        return EnumGui.ENGINE_STEAM;
    }

    @Override
    public long getMaxOutputMJ() {
        return OUTPUT_MJ;
    }

    @Override
    public int steamUsedPerTick() {
        return 20;
    }

    @Override
    public long maxEnergy() {
        return CAPACITY;
    }

    @Override
    public long maxEnergyReceived() {
        return RECEIVE;
    }

    @Override
    public long maxEnergyExtracted() {
        return EXTRACT;
    }
}
