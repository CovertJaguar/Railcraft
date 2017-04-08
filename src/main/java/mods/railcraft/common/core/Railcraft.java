/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2016
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.core;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.primitives.Ints;
import mods.railcraft.api.crafting.ICrusherCraftingManager;
import mods.railcraft.api.crafting.RailcraftCraftingManager;
import mods.railcraft.api.fuel.FuelManager;
import mods.railcraft.api.tracks.TrackRegistry;
import mods.railcraft.common.carts.LinkageManager;
import mods.railcraft.common.commands.RootCommand;
import mods.railcraft.common.modules.RailcraftModuleManager;
import mods.railcraft.common.plugins.craftguide.CraftGuidePlugin;
import mods.railcraft.common.plugins.forge.DataManagerPlugin;
import mods.railcraft.common.util.inventory.filters.StandardStackFilters;
import mods.railcraft.common.util.misc.BallastRegistry;
import mods.railcraft.common.util.misc.BlinkTick;
import mods.railcraft.common.util.misc.Game;
import mods.railcraft.common.util.network.PacketHandler;
import net.minecraft.block.Block;
import net.minecraft.command.CommandHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import org.apache.logging.log4j.Level;

import java.io.File;

@SuppressWarnings("unused")
@Mod(modid = Railcraft.MOD_ID, name = Railcraft.NAME,
        version = Railcraft.VERSION,
        certificateFingerprint = "a0c255ac501b2749537d5824bb0f0588bf0320fa",
        acceptedMinecraftVersions = Railcraft.MC_VERSION,
        guiFactory = "mods.railcraft.client.core.RailcraftGuiConfigFactory",
        updateJSON = "http://www.railcraft.info/railcraft_versions",
        dependencies = "required-after:Forge@[12.18.2.2107,);"
                + "after:BuildCraft|Core[6.1.7,);"
                + "after:BuildCraft|Energy;"
                + "after:BuildCraft|Builders;"
                + "after:BuildCraft|Factory;"
                + "after:BuildCraftAPI|statements[1.0,);"
                + "after:BuildCraftAPI|transport[1.0,);"
                + "after:forestry[5.2.15,);"
                + "after:Thaumcraft;"
                + "after:IC2@[2.6.9-ex192,)")
public final class Railcraft {
    public static final String NAME = "Railcraft";
    public static final String MOD_ID = "railcraft";
    public static final String MC_VERSION = "[1.10.2,1.11)";
    public static final RootCommand rootCommand = new RootCommand();
    static final String VERSION = "@VERSION@";
    @Instance(Railcraft.MOD_ID)
    public static Railcraft instance;
    //    public int totalMultiBlockUpdates = 0;
//    public int ticksSinceLastMultiBlockPrint = 0;
    @SidedProxy(clientSide = "mods.railcraft.client.core.ClientProxy", serverSide = "mods.railcraft.common.core.CommonProxy")
    public static CommonProxy proxy;
    private File configFolder;

    public static CommonProxy getProxy() {
        return proxy;
    }

    public static Railcraft getMod() {
        return instance;
    }

    public static String getVersion() {
        return VERSION;
    }

    public File getConfigFolder() {
        return configFolder;
    }

    @Mod.EventHandler
    public void processIMCRequests(FMLInterModComms.IMCEvent event) {
        Splitter splitter = Splitter.on("@").trimResults();
        for (FMLInterModComms.IMCMessage mess : event.getMessages()) {
            if (mess.key.equals("ballast")) {
                String[] tokens = Iterables.toArray(splitter.split(mess.getStringValue()), String.class);
                if (tokens.length != 2) {
                    Game.log(Level.WARN, String.format("Mod %s attempted to register a ballast, but failed: %s", mess.getSender(), mess.getStringValue()));
                    continue;
                }
                String blockName = tokens[0];
                Integer metadata = Ints.tryParse(tokens[1]);
                if (blockName == null || metadata == null) {
                    Game.log(Level.WARN, String.format("Mod %s attempted to register a ballast, but failed: %s", mess.getSender(), mess.getStringValue()));
                    continue;
                }
                BallastRegistry.registerBallast(Block.getBlockFromName(blockName), metadata);
                Game.log(Level.DEBUG, String.format("Mod %s registered %s as a valid ballast", mess.getSender(), mess.getStringValue()));
            } else if (mess.key.equals("boiler-fuel-liquid")) {
                String[] tokens = Iterables.toArray(splitter.split(mess.getStringValue()), String.class);
                if (tokens.length != 2) {
                    Game.log(Level.WARN, String.format("Mod %s attempted to register a liquid Boiler fuel, but failed: %s", mess.getSender(), mess.getStringValue()));
                    continue;
                }
                Fluid fluid = FluidRegistry.getFluid(tokens[0]);
                Integer fuel = Ints.tryParse(tokens[1]);
                if (fluid == null || fuel == null) {
                    Game.log(Level.WARN, String.format("Mod %s attempted to register a liquid Boiler fuel, but failed: %s", mess.getSender(), mess.getStringValue()));
                    continue;
                }
                FuelManager.addBoilerFuel(fluid, fuel);
                Game.log(Level.DEBUG, String.format("Mod %s registered %s as a valid liquid Boiler fuel", mess.getSender(), mess.getStringValue()));
            } else if (mess.key.equals("rock-crusher")) {
                NBTTagCompound nbt = mess.getNBTValue();
                ItemStack input = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("input"));
                ICrusherCraftingManager.ICrusherRecipe recipe = RailcraftCraftingManager.rockCrusher.createAndAddRecipe(input, nbt.getBoolean("matchMeta"), nbt.getBoolean("matchNBT"));
                for (int i = 0; i < 9; i++) {
                    if (nbt.hasKey("output" + i)) {
                        NBTTagCompound outputNBT = nbt.getCompoundTag("output" + i);
                        recipe.addOutput(ItemStack.loadItemStackFromNBT(outputNBT), outputNBT.getFloat("chance"));
                    }
                }
            } else if (mess.key.equals("high-speed-explosion-excluded-entities")) {
                NBTTagCompound nbt = mess.getNBTValue();
                if (nbt.hasKey("entities")) {
                    String entities = nbt.getString("entities");
                    Iterable<String> split = splitter.split(entities);
                    RailcraftConfig.excludedAllEntityFromHighSpeedExplosions(split);
                } else {
                    Game.log(Level.WARN, "Mod %s attempted to exclude an entity from H.S. explosions, but failed: %s", mess.getSender(), nbt);
                }
            }
        }
    }

    @Mod.EventHandler
    public void fingerprintError(FMLFingerprintViolationEvent event) {
        if (Game.isObfuscated()) {
            Game.logErrorFingerprint(MOD_ID);
//            FMLCommonHandler.instance().exitJava(1, false);
            throw new RuntimeException("Invalid Fingerprint");
        }
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
//        Game.log(Level.FINE, "Pre-Init Phase");

        RailcraftModuleManager.loadModules(event.getAsmData());

        configFolder = new File(event.getModConfigurationDirectory(), "railcraft");
        RailcraftConfig.preInit();

        PacketHandler.init();
        DataManagerPlugin.register();

        StandardStackFilters.initialize();

        RailcraftModuleManager.preInit();

        TrackRegistry.TRACK_TYPE.finalizeRegistry();
        TrackRegistry.TRACK_KIT.finalizeRegistry();

        proxy.initializeClient();

        FMLInterModComms.sendMessage("OpenBlocks", "donateUrl", "http://www.railcraft.info/donate/");
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
//        Game.log(Level.FINE, "Init Phase");

        RailcraftModuleManager.init();

        MinecraftForge.EVENT_BUS.register(new BlinkTick());
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
//        Game.log(Level.FINE, "Post-Init Phase");
        RailcraftModuleManager.postInit();

        proxy.finalizeClient();

        CraftGuidePlugin.init();

        RailcraftConfig.postInit();
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        CommandHandler commandManager = (CommandHandler) event.getServer().getCommandManager();
        commandManager.registerCommand(rootCommand);
    }

    @Mod.EventHandler
    public void serverCleanUp(FMLServerStoppingEvent event) {
        LinkageManager.reset();
    }

    @Mod.EventHandler
    public void missingMapping(FMLMissingMappingsEvent event) {
        Remapper.handle(event);
    }

}
