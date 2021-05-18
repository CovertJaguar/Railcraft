/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2020
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.modules;

import mods.railcraft.api.core.RailcraftModule;
import mods.railcraft.common.blocks.RailcraftBlocks;
import mods.railcraft.common.plugins.buildcraft.power.MjPlugin;
import mods.railcraft.common.plugins.ic2.IC2Plugin;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Loader;

/**
 * @author CovertJaguar <http://www.railcraft.info>
 */
@RailcraftModule(value = "railcraft:steam", description = "engines, boilers, steam traps")
public class ModuleSteam extends RailcraftModulePayload {
    public static Config config;

    public ModuleSteam() {
        add(
                RailcraftBlocks.ENGINE_HOBBY,
                RailcraftBlocks.ENGINE_LOW,
                RailcraftBlocks.ENGINE_HIGH,
                RailcraftBlocks.EQUIPMENT,
                RailcraftBlocks.ADMIN_STEAM_PRODUCER,
                RailcraftBlocks.BOILER_FIREBOX_FLUID,
                RailcraftBlocks.BOILER_FIREBOX_SOLID,
                RailcraftBlocks.BOILER_TANK_PRESSURE_HIGH,
                RailcraftBlocks.BOILER_TANK_PRESSURE_LOW
        );
        setEnabledEventHandler(new ModuleEventHandler() {
            @Override
            public void preInit() {
                MjPlugin.LOADED = Loader.isModLoaded("buildcraftlib");
            }

            @Override
            public void postInit() {
                IC2Plugin.nerfSyntheticCoal();
            }
        });
    }

    @Override
    public void loadConfig(Configuration config) {
        ModuleSteam.config = new Config(config);
    }

    public static class Config {
        public final float fuelMultiplier;
        public final float biofuelMultiplier;
        public final float fuelPerSteamMultiplier;

        public Config(Configuration config) {
            fuelMultiplier = config.getFloat("fuelMultiplier", CAT_CONFIG, 1.0F, 0.2F, 10F,
                    "adjust the heat value of Fuel in a Boiler");
            biofuelMultiplier = config.getFloat("biofuelMultiplier", CAT_CONFIG, 1.0F, 0.2F, 10F,
                    "adjust the heat value of BioFuel in a Boiler");
            fuelPerSteamMultiplier = config.getFloat("fuelPerSteamMultiplier", CAT_CONFIG, 1.0F, 0.2F, 6.0F,
                    "adjust the amount of fuel used to create Steam");
        }
    }
}
