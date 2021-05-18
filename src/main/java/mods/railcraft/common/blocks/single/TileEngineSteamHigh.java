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
public class TileEngineSteamHigh extends TileEngineSteam {

    private static final long OUTPUT_MJ = 8 * MjPlugin.MJ;
    private static final long CAPACITY = 30000 * MjPlugin.MJ;
    private static final long RECEIVE = 1200 * MjPlugin.MJ;
    private static final long EXTRACT = 160 * MjPlugin.MJ;

    @Override
    public EnumGui getGui() {
        return EnumGui.ENGINE_HOBBY;
    }

    @Override
    public long getMaxOutputMJ() {
        return OUTPUT_MJ;
    }

    @Override
    public int steamUsedPerTick() {
        return 40;
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
