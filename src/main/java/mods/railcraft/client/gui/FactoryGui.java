/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2017
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.client.gui;

import mods.railcraft.common.blocks.RailcraftTileEntity;
import mods.railcraft.common.blocks.detector.TileDetector;
import mods.railcraft.common.blocks.machine.ITankTile;
import mods.railcraft.common.blocks.machine.TileMultiBlock;
import mods.railcraft.common.blocks.machine.alpha.*;
import mods.railcraft.common.blocks.machine.beta.TileBoilerFireboxFluid;
import mods.railcraft.common.blocks.machine.beta.TileBoilerFireboxSolid;
import mods.railcraft.common.blocks.machine.beta.TileEngineSteam;
import mods.railcraft.common.blocks.machine.beta.TileEngineSteamHobby;
import mods.railcraft.common.blocks.machine.equipment.TileFeedStation;
import mods.railcraft.common.blocks.machine.equipment.TileRollingMachine;
import mods.railcraft.common.blocks.machine.equipment.TileRollingMachinePowered;
import mods.railcraft.common.blocks.machine.interfaces.ITileAspectResponder;
import mods.railcraft.common.blocks.machine.manipulator.*;
import mods.railcraft.common.blocks.machine.wayobjects.actuators.TileActuatorMotor;
import mods.railcraft.common.blocks.machine.wayobjects.boxes.TileBoxAnalog;
import mods.railcraft.common.blocks.machine.wayobjects.boxes.TileBoxCapacitor;
import mods.railcraft.common.blocks.machine.wayobjects.boxes.TileBoxController;
import mods.railcraft.common.blocks.machine.worldspike.TileWorldspike;
import mods.railcraft.common.blocks.tracks.outfitted.TileTrackOutfitted;
import mods.railcraft.common.blocks.tracks.outfitted.kits.TrackKitEmbarking;
import mods.railcraft.common.blocks.tracks.outfitted.kits.TrackKitLauncher;
import mods.railcraft.common.blocks.tracks.outfitted.kits.TrackKitPriming;
import mods.railcraft.common.blocks.tracks.outfitted.kits.TrackKitRouting;
import mods.railcraft.common.carts.*;
import mods.railcraft.common.gui.EnumGui;
import mods.railcraft.common.gui.containers.ContainerTrackDumping;
import mods.railcraft.common.gui.containers.FactoryContainer;
import mods.railcraft.common.plugins.forge.LocalizationPlugin;
import mods.railcraft.common.util.misc.Game;
import mods.railcraft.common.util.routing.IRouter;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;

/**
 * @author CovertJaguar <http://www.railcraft.info>
 */
@SideOnly(Side.CLIENT)
public class FactoryGui {

    @Nullable
    public static GuiScreen build(EnumGui gui, InventoryPlayer inv, @Nullable Object obj, World world, int x, int y, int z) {
        if (gui != EnumGui.ANVIL && obj == null)
            return null;

        if (obj instanceof TileMultiBlock && !((TileMultiBlock) obj).isStructureValid())
            return null;

        try {
            switch (gui) {
                case MANIPULATOR_ITEM:
                    return new GuiManipulatorCartItem(inv, (TileItemManipulator) obj);
                case MANIPULATOR_FLUID:
                    return new GuiManipulatorCartFluid(inv, (TileFluidManipulator) obj);
                case LOADER_ENERGY:
                    return new GuiManipulatorCartIC2Loader(inv, (TileIC2Loader) obj);
                case UNLOADER_ENERGY:
                    return new GuiManipulatorCartIC2Unloader(inv, (TileIC2Unloader) obj);
                case MANIPULATOR_RF:
                    return new GuiManipulatorCartRF((TileRFManipulator) obj);
                case DETECTOR_ITEM:
                    return new GuiDetectorItem(inv, (TileDetector) obj);
                case DETECTOR_TANK:
                    return new GuiDetectorTank(inv, (TileDetector) obj);
                case DETECTOR_SHEEP:
                    return new GuiDetectorSheep(inv, (TileDetector) obj);
                case DETECTOR_ANIMAL:
                    return new GuiDetectorAnimal((TileDetector) obj);
                case DETECTOR_ADVANCED:
                    return new GuiDetectorAdvanced(inv, (TileDetector) obj);
                case DETECTOR_TRAIN:
                    return new GuiDetectorTrain((TileDetector) obj);
                case DETECTOR_VILLAGER:
                    return new GuiDetectorVillager((TileDetector) obj);
                case DETECTOR_LOCOMOTIVE:
                    return new GuiDetectorLocomotive(inv, (TileDetector) obj);
                case DETECTOR_ROUTING:
                    return new GuiRouting(inv, (RailcraftTileEntity) obj, (IRouter) ((TileDetector) obj).getDetector());
                case CART_DISPENSER:
                    return new GuiDispenserCart(inv, (TileDispenserCart) obj);
                case TRAIN_DISPENSER:
                    return new GuiDispenserTrain(inv, (TileDispenserTrain) obj);
                case COKE_OVEN:
                    return new GuiCokeOven(inv, (TileCokeOven) obj);
                case BLAST_FURNACE:
                    return new GuiBlastFurnace(inv, (TileBlastFurnace) obj);
                case STEAN_OVEN:
                    return new GuiSteamOven(inv, (TileSteamOven) obj);
                case TANK:
                    return new GuiTank(inv, (ITankTile) obj);
                case ROCK_CRUSHER:
                    return new GuiRockCrusher(inv, (TileRockCrusher) obj);
                case ROLLING_MACHINE_MANUAL:
                    return new GuiRollingMachine(inv, (TileRollingMachine) obj);
                case ROLLING_MACHINE_POWERED:
                    return new GuiRollingMachinePowered(inv, (TileRollingMachinePowered) obj);
                case FEED_STATION:
                    return new GuiFeedStation(inv, (TileFeedStation) obj);
                case TRADE_STATION:
                    return new GuiTradeStation(inv, (TileTradeStation) obj);
                case WORLDSPIKE:
                    return new GuiWorldspike(inv, (TileWorldspike) obj);
                case ENGINE_STEAM:
                    return new GuiEngineSteam(inv, (TileEngineSteam) obj);
                case ENGINE_HOBBY:
                    return new GuiEngineSteamHobby(inv, (TileEngineSteamHobby) obj);
                case BOILER_SOLID:
                    return new GuiBoilerSolid(inv, (TileBoilerFireboxSolid) obj);
                case BOILER_LIQUID:
                    return new GuiBoilerFluid(inv, (TileBoilerFireboxFluid) obj);
                case TURBINE:
                    return new GuiTurbine(inv, (TileSteamTurbine) obj);
                case ANVIL:
                    return new GuiAnvil(inv, world, new BlockPos(x, y, z));
                case ROUTING:
                    return new GuiRouting(inv, (RailcraftTileEntity) obj, (IRouter) obj);
                case TRACK_ROUTING:
                    return new GuiTrackRouting(inv, (TrackKitRouting) ((TileTrackOutfitted) obj).getTrackKitInstance());
                case SWITCH_MOTOR:
                    return new GuiActuatorMotor(inv.player, (TileActuatorMotor) obj, LocalizationPlugin.translate("gui.railcraft.switch.motor.action"));
                case BOX_RECEIVER:
                    return new GuiAspectAction(inv.player, (ITileAspectResponder) obj, LocalizationPlugin.translate("gui.railcraft.box.aspect.action"));
                case BOX_RELAY:
                    return new GuiAspectAction(inv.player, (ITileAspectResponder) obj, LocalizationPlugin.translate("gui.railcraft.box.aspect.action"));
                case BOX_CONTROLLER:
                    return new GuiBoxController((TileBoxController) obj);
                case BOX_ANALOG_CONTROLLER:
                    return new GuiBoxAnalogController((TileBoxAnalog) obj);
                case BOX_CAPACITOR:
                    return new GuiBoxCapacitor((TileBoxCapacitor) obj);
                case TRACK_LAUNCHER:
                    return new GuiTrackLauncher((TrackKitLauncher) ((TileTrackOutfitted) obj).getTrackKitInstance());
                case TRACK_PRIMING:
                    return new GuiTrackPriming((TrackKitPriming) ((TileTrackOutfitted) obj).getTrackKitInstance());
                case TRACK_EMBARKING:
                    return new GuiTrackEmbarking((TrackKitEmbarking) ((TileTrackOutfitted) obj).getTrackKitInstance());
                case CART_BORE:
                    return new GuiCartBore(inv, (EntityTunnelBore) obj);
                case CART_ENERGY:
                    return new GuiCartEnergy(inv, (IIC2EnergyCart) obj);
                case CART_RF:
                    return new GuiCartRF((EntityCartRF) obj);
                case CART_TANK:
                    return new GuiCartTank(inv, (EntityCartTank) obj);
                case CART_CARGO:
                    return new GuiCartCargo(inv, (EntityCartCargo) obj);
                case CART_WORLDSPIKE:
                    return new GuiCartWorldspike(inv, (EntityCartWorldspike) obj);
                case CART_TNT_FUSE:
                    return new GuiCartTNTFuse((CartBaseExplosive) obj);
                case CART_WORK:
                    return new GuiCartWork(inv, (EntityCartWork) obj);
                case CART_TRACK_LAYER:
                    return new GuiCartTrackLayer(inv, (EntityCartTrackLayer) obj);
                case CART_TRACK_RELAYER:
                    return new GuiCartTrackRelayer(inv, (EntityCartTrackRelayer) obj);
                case CART_UNDERCUTTER:
                    return new GuiCartUndercutter(inv, (EntityCartUndercutter) obj);
                case LOCO_STEAM:
                    return new GuiLocomotiveSteamSolid(inv, (EntityLocomotiveSteamSolid) obj);
                case LOCO_ELECTRIC:
                    return new GuiLocomotiveElectric(inv, (EntityLocomotiveElectric) obj);
                case LOCO_CREATIVE:
                    return new GuiLocomotiveCreative(inv, (EntityLocomotiveCreative) obj);
                case TRACK_DUMPING:
                    return new GuiTrackDumping((ContainerTrackDumping) FactoryContainer.build(gui, inv, obj, world, x, y, z));
                default:
                    Game.log(Level.ERROR, "Failed to retrieve a gui {0} at ({1},{2},{3})!", gui, x, y, z);
                    if (Game.DEVELOPMENT_ENVIRONMENT)
                        throw new RuntimeException("Building gui " + gui + " failed at x=" + x + ", y=" + y + ", z=" + z);
                    //TODO: Fix this
//                    return RailcraftModuleManager.getGuiScreen(gui, inv, obj, world, x, y, z);
            }
        } catch (ClassCastException ex) {
            Game.log(Level.WARN, "Error when attempting to build gui {0}: {1}", gui, ex);
        }
        return null;
    }

}
