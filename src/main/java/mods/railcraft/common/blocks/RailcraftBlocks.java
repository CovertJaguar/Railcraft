/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2020
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/

package mods.railcraft.common.blocks;

import mods.railcraft.api.core.IVariantEnum;
import mods.railcraft.common.blocks.aesthetics.BlockSimpleSlab;
import mods.railcraft.common.blocks.aesthetics.ItemRailcraftSlab;
import mods.railcraft.common.blocks.aesthetics.brick.*;
import mods.railcraft.common.blocks.aesthetics.concrete.BlockReinforcedConcrete;
import mods.railcraft.common.blocks.aesthetics.generic.BlockGeneric;
import mods.railcraft.common.blocks.aesthetics.generic.ItemBlockGeneric;
import mods.railcraft.common.blocks.aesthetics.glass.BlockStrengthGlass;
import mods.railcraft.common.blocks.aesthetics.materials.BlockLantern;
import mods.railcraft.common.blocks.aesthetics.materials.ItemMaterial;
import mods.railcraft.common.blocks.aesthetics.metals.BlockMetal;
import mods.railcraft.common.blocks.aesthetics.metals.ItemBlockMetal;
import mods.railcraft.common.blocks.aesthetics.post.*;
import mods.railcraft.common.blocks.anvil.BlockRCAnvil;
import mods.railcraft.common.blocks.anvil.ItemAnvil;
import mods.railcraft.common.blocks.charge.*;
import mods.railcraft.common.blocks.detector.BlockDetector;
import mods.railcraft.common.blocks.detector.ItemDetector;
import mods.railcraft.common.blocks.logbook.BlockLogbook;
import mods.railcraft.common.blocks.machine.ItemMachine;
import mods.railcraft.common.blocks.machine.charge.BlockChargeFeeder;
import mods.railcraft.common.blocks.machine.equipment.BlockMachineEquipment;
import mods.railcraft.common.blocks.machine.manipulator.BlockMachineManipulator;
import mods.railcraft.common.blocks.machine.wayobjects.actuators.BlockMachineActuator;
import mods.railcraft.common.blocks.machine.wayobjects.boxes.BlockMachineSignalBoxRailcraft;
import mods.railcraft.common.blocks.machine.wayobjects.signals.BlockMachineSignalDualRailcraft;
import mods.railcraft.common.blocks.machine.wayobjects.signals.BlockMachineSignalRailcraft;
import mods.railcraft.common.blocks.machine.wayobjects.signals.ItemSignal;
import mods.railcraft.common.blocks.machine.worldspike.BlockWorldspike;
import mods.railcraft.common.blocks.machine.worldspike.BlockWorldspikePoint;
import mods.railcraft.common.blocks.machine.worldspike.ItemWorldspike;
import mods.railcraft.common.blocks.ore.*;
import mods.railcraft.common.blocks.simple.*;
import mods.railcraft.common.blocks.single.*;
import mods.railcraft.common.blocks.structures.*;
import mods.railcraft.common.blocks.tracks.ItemTrack;
import mods.railcraft.common.blocks.tracks.behaivor.TrackTypes;
import mods.railcraft.common.blocks.tracks.elevator.BlockTrackElevator;
import mods.railcraft.common.blocks.tracks.flex.BlockTrackFlex;
import mods.railcraft.common.blocks.tracks.flex.BlockTrackFlexAbandoned;
import mods.railcraft.common.blocks.tracks.flex.BlockTrackFlexElectric;
import mods.railcraft.common.blocks.tracks.flex.ItemTrackStateless;
import mods.railcraft.common.blocks.tracks.force.BlockTrackForce;
import mods.railcraft.common.blocks.tracks.outfitted.BlockTrackOutfitted;
import mods.railcraft.common.blocks.tracks.outfitted.ItemTrackOutfitted;
import mods.railcraft.common.core.IRailcraftObjectContainer;
import mods.railcraft.common.core.RailcraftConfig;
import mods.railcraft.common.items.IRailcraftItemSimple;
import mods.railcraft.common.items.firestone.BlockRitual;
import mods.railcraft.common.plugins.forge.RailcraftRegistry;
import mods.railcraft.common.util.inventory.InvTools;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by CovertJaguar on 4/13/2016 for Railcraft.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public enum RailcraftBlocks implements IRailcraftBlockContainer {
    ACTUATOR(BlockDef.build("actuator", BlockMachineActuator.class).item(ItemMachine::new)),
    ANVIL_STEEL(BlockDef.build("anvil", BlockRCAnvil.class).item(ItemAnvil::new)),
    BATTERY_NICKEL_IRON(BlockDef.build("battery_nickel_iron", BlockBatteryNickelIron.class).item(ItemBattery::new)),
    BATTERY_NICKEL_ZINC(BlockDef.build("battery_nickel_zinc", BlockBatteryNickelZinc.class).item(ItemBattery::new)),
    BATTERY_ZINC_CARBON(BlockDef.build("battery_zinc_carbon", BlockBatteryZincCarbon.class).item(ItemBattery::new)),
    BATTERY_ZINC_SILVER(BlockDef.build("battery_zinc_silver", BlockBatteryZincSilver.class).item(ItemBattery::new)),

    CREOSOTE_BLOCK(BlockDef.build("creosote_block", BlockCreosote.class).item(ItemBlockCreosote::new)),
    CREOSOTE_STAIRS(BlockDef.build("creosote_stairs", BlockRailcraftStairs.class)
            .block(() -> new BlockRailcraftStairs(byTag("creosote_block").getDefaultState()))
            .defaultItem()
            .condition(CREOSOTE_BLOCK)
    ),
    CREOSOTE_DOUBLE_SLAB(BlockDef.build("creosote_double_slab", BlockSimpleSlab.class)
            .block(() -> new BlockSimpleSlab.Double(CREOSOTE_BLOCK.getDefaultState()))
            .condition(CREOSOTE_BLOCK)
    ),
    CREOSOTE_SLAB(BlockDef.build("creosote_slab", BlockSimpleSlab.class)
            .block(() -> new BlockSimpleSlab.Half(CREOSOTE_BLOCK.getDefaultState()))
            .item(block -> new ItemRailcraftSlab(block, block, byTag("creosote_double_slab")))
            .condition(CREOSOTE_BLOCK)
    ),

    COKE_BLOCK(BlockDef.build("coke_block", BlockCoke.class).item(ItemBlockCoke::new)),

    ABYSSAL_STONE(BlockDef.build("abyssal_stone", BlockRailcraftStone.class)
            .block(() -> new BlockRailcraftStone(MapColor.BLACK))
            .defaultItem()
    ),
    QUARRIED_STONE(BlockDef.build("quarried_stone", BlockRailcraftStone.class)
            .block(() -> new BlockRailcraftStone(MapColor.SNOW))
            .defaultItem()
    ),

    // Bricks

    ABYSSAL_BRICK(BlockDef.build("abyssal_brick", BlockBrick.class).block(() -> new BlockBrick(BrickTheme.ABYSSAL)).defaultSubtypedItem()
            .condition(ABYSSAL_STONE)
    ),
    BLEACHED_BONE_BRICK(BlockDef.build("bleachedbone_brick", BlockBrick.class).block(() -> new BlockBrick(BrickTheme.BLEACHEDBONE)).defaultSubtypedItem()),
    BLOOD_STAINED_BRICK(BlockDef.build("bloodstained_brick", BlockBrick.class).block(() -> new BlockBrick(BrickTheme.BLOODSTAINED)).defaultSubtypedItem()),
    FROST_BOUND_BRICK(BlockDef.build("frostbound_brick", BlockBrick.class).block(() -> new BlockBrick(BrickTheme.FROSTBOUND)).defaultSubtypedItem()),
    INFERNAL_BRICK(BlockDef.build("infernal_brick", BlockBrick.class).block(() -> new BlockBrick(BrickTheme.INFERNAL)).defaultSubtypedItem()),
    PEARLIZED_BRICK(BlockDef.build("pearlized_brick", BlockBrick.class).block(() -> new BlockBrick(BrickTheme.PEARLIZED)).defaultSubtypedItem()),
    QUARRIED_BRICK(BlockDef.build("quarried_brick", BlockBrick.class).block(() -> new BlockBrick(BrickTheme.QUARRIED)).defaultSubtypedItem()
            .condition(QUARRIED_STONE)
    ),
    BADLANDS_BRICK(BlockDef.build("badlands_brick", BlockBrick.class).block(() -> new BlockBrick(BrickTheme.BADLANDS)).defaultSubtypedItem()),
    SANDY_BRICK(BlockDef.build("sandy_brick", BlockBrick.class).block(() -> new BlockBrick(BrickTheme.SANDY)).defaultSubtypedItem()),

    // Vanilla Bricks

    ANDESITE_BRICK(BlockDef.build("andesite_brick", BlockBrick.class).block(() -> new BlockBrick(BrickTheme.ANDESITE)).defaultSubtypedItem()),
    DIORITE_BRICK(BlockDef.build("diorite_brick", BlockBrick.class).block(() -> new BlockBrick(BrickTheme.DIORITE)).defaultSubtypedItem()),
    GRANITE_BRICK(BlockDef.build("granite_brick", BlockBrick.class).block(() -> new BlockBrick(BrickTheme.GRANITE)).defaultSubtypedItem()),
    NETHER_BRICK(BlockDef.build("nether_brick", BlockBrick.class).block(() -> new BlockBrick(BrickTheme.NETHER)).defaultSubtypedItem()),
    RED_NETHER_BRICK(BlockDef.build("red_nether_brick", BlockBrick.class).block(() -> new BlockBrick(BrickTheme.RED_NETHER)).defaultSubtypedItem()),

    // Slabs
    ABYSSAL_DOUBLE_SLAB(BlockDef.build("abyssal_double_slab", BlockBrickSlab.class)
            .block(() -> new BlockBrickSlab.Double(BrickTheme.ABYSSAL))
            .condition(ABYSSAL_BRICK)
    ),
    ABYSSAL_SLAB(BlockDef.build("abyssal_slab", BlockBrickSlab.class)
            .block(() -> new BlockBrickSlab.Half(BrickTheme.ABYSSAL))
            .item(block -> new ItemRailcraftSlab(block, block, byTag("abyssal_double_slab")))
            .condition(ABYSSAL_BRICK)
    ),

    BADLANDS_DOUBLE_SLAB(BlockDef.build("badlands_double_slab", BlockBrickSlab.class)
            .block(() -> new BlockBrickSlab.Double(BrickTheme.BADLANDS))
            .condition(BADLANDS_BRICK)
    ),
    BADLANDS_SLAB(BlockDef.build("badlands_slab", BlockBrickSlab.class)
            .block(() -> new BlockBrickSlab.Half(BrickTheme.BADLANDS))
            .item(block -> new ItemRailcraftSlab(block, block, byTag("badlands_double_slab")))
            .condition(BADLANDS_BRICK)
    ),

    BLEACHED_BONE_DOUBLE_SLAB(BlockDef.build("bleachedbone_double_slab", BlockBrickSlab.class)
            .block(() -> new BlockBrickSlab.Double(BrickTheme.BLEACHEDBONE))
            .condition(BLEACHED_BONE_BRICK)
    ),
    BLEACHED_BONE_SLAB(BlockDef.build("bleachedbone_slab", BlockBrickSlab.class)
            .block(() -> new BlockBrickSlab.Half(BrickTheme.BLEACHEDBONE))
            .item(block -> new ItemRailcraftSlab(block, block, byTag("bleachedbone_double_slab")))
            .condition(BLEACHED_BONE_BRICK)
    ),

    BLOOD_STAINED_DOUBLE_SLAB(BlockDef.build("bloodstained_double_slab", BlockBrickSlab.class)
            .block(() -> new BlockBrickSlab.Double(BrickTheme.BLOODSTAINED))
            .condition(BLOOD_STAINED_BRICK)
    ),
    BLOOD_STAINED_SLAB(BlockDef.build("bloodstained_slab", BlockBrickSlab.class)
            .block(() -> new BlockBrickSlab.Half(BrickTheme.BLOODSTAINED))
            .item(block -> new ItemRailcraftSlab(block, block, byTag("bloodstained_double_slab")))
            .condition(BLOOD_STAINED_BRICK)
    ),

    FROST_BOUND_DOUBLE_SLAB(BlockDef.build("frostbound_double_slab", BlockBrickSlab.class)
            .block(() -> new BlockBrickSlab.Double(BrickTheme.FROSTBOUND))
            .condition(FROST_BOUND_BRICK)
    ),
    FROST_BOUND_SLAB(BlockDef.build("frostbound_slab", BlockBrickSlab.class)
            .block(() -> new BlockBrickSlab.Half(BrickTheme.FROSTBOUND))
            .item(block -> new ItemRailcraftSlab(block, block, byTag("frostbound_double_slab")))
            .condition(FROST_BOUND_BRICK)
    ),

    INFERNAL_DOUBLE_SLAB(BlockDef.build("infernal_double_slab", BlockBrickSlab.class)
            .block(() -> new BlockBrickSlab.Double(BrickTheme.INFERNAL))
            .condition(INFERNAL_BRICK)
    ),
    INFERNAL_SLAB(BlockDef.build("infernal_slab", BlockBrickSlab.class)
            .block(() -> new BlockBrickSlab.Half(BrickTheme.INFERNAL))
            .item(block -> new ItemRailcraftSlab(block, block, byTag("infernal_double_slab")))
            .condition(INFERNAL_BRICK)
    ),

    PEARLIZED_DOUBLE_SLAB(BlockDef.build("pearlized_double_slab", BlockBrickSlab.class)
            .block(() -> new BlockBrickSlab.Double(BrickTheme.PEARLIZED))
            .condition(PEARLIZED_BRICK)
    ),
    PEARLIZED_SLAB(BlockDef.build("pearlized_slab", BlockBrickSlab.class)
            .block(() -> new BlockBrickSlab.Half(BrickTheme.PEARLIZED))
            .item(block -> new ItemRailcraftSlab(block, block, byTag("pearlized_double_slab")))
            .condition(PEARLIZED_BRICK)
    ),

    QUARRIED_DOUBLE_SLAB(BlockDef.build("quarried_double_slab", BlockBrickSlab.class)
            .block(() -> new BlockBrickSlab.Double(BrickTheme.QUARRIED))
            .condition(QUARRIED_BRICK)
    ),
    QUARRIED_SLAB(BlockDef.build("quarried_slab", BlockBrickSlab.class)
            .block(() -> new BlockBrickSlab.Half(BrickTheme.QUARRIED))
            .item(block -> new ItemRailcraftSlab(block, block, byTag("quarried_double_slab")))
            .condition(QUARRIED_BRICK)
    ),

    SANDY_DOUBLE_SLAB(BlockDef.build("sandy_double_slab", BlockBrickSlab.class)
            .block(() -> new BlockBrickSlab.Double(BrickTheme.SANDY))
            .condition(SANDY_BRICK)
    ),
    SANDY_SLAB(BlockDef.build("sandy_slab", BlockBrickSlab.class)
            .block(() -> new BlockBrickSlab.Half(BrickTheme.SANDY))
            .item(block -> new ItemRailcraftSlab(block, block, byTag("sandy_double_slab")))
            .condition(SANDY_BRICK)
    ),

    // Stairs

    ABYSSAL_BRICK_STAIRS(BlockDef.build("abyssal_brick_stairs", BlockBrickStairs.class)
            .block(() -> new BlockBrickStairs(BrickTheme.ABYSSAL, BrickVariant.BRICK))
            .defaultItem()
            .condition(ABYSSAL_STONE)
            .condition(ABYSSAL_BRICK)
    ),
    ABYSSAL_PAVER_STAIRS(BlockDef.build("abyssal_paver_stairs", BlockBrickStairs.class)
            .block(() -> new BlockBrickStairs(BrickTheme.ABYSSAL, BrickVariant.PAVER))
            .defaultItem()
            .condition(ABYSSAL_STONE)
            .condition(ABYSSAL_BRICK)
    ),
    BADLANDS_BRICK_STAIRS(BlockDef.build("badlands_brick_stairs", BlockBrickStairs.class)
            .block(() -> new BlockBrickStairs(BrickTheme.BADLANDS, BrickVariant.BRICK))
            .defaultItem()
            .condition(BADLANDS_BRICK)
    ),
    BADLANDS_PAVER_STAIRS(BlockDef.build("badlands_paver_stairs", BlockBrickStairs.class)
            .block(() -> new BlockBrickStairs(BrickTheme.BADLANDS, BrickVariant.PAVER))
            .defaultItem()
            .condition(BADLANDS_BRICK)
    ),
    BLEACHED_BONE_BRICK_STAIRS(BlockDef.build("bleachedbone_brick_stairs", BlockBrickStairs.class)
            .block(() -> new BlockBrickStairs(BrickTheme.BLEACHEDBONE, BrickVariant.BRICK))
            .defaultItem()
            .condition(BLEACHED_BONE_BRICK)
    ),
    BLEACHED_BONE_PAVER_STAIRS(BlockDef.build("bleachedbone_paver_stairs", BlockBrickStairs.class)
            .block(() -> new BlockBrickStairs(BrickTheme.BLEACHEDBONE, BrickVariant.PAVER))
            .defaultItem()
            .condition(BLEACHED_BONE_BRICK)
    ),
    BLOOD_STAINED_BRICK_STAIRS(BlockDef.build("bloodstained_brick_stairs", BlockBrickStairs.class)
            .block(() -> new BlockBrickStairs(BrickTheme.BLOODSTAINED, BrickVariant.BRICK))
            .defaultItem()
            .condition(BLOOD_STAINED_BRICK)
    ),
    BLOOD_STAINED_PAVER_STAIRS(BlockDef.build("bloodstained_paver_stairs", BlockBrickStairs.class)
            .block(() -> new BlockBrickStairs(BrickTheme.BLOODSTAINED, BrickVariant.PAVER))
            .defaultItem()
            .condition(BLOOD_STAINED_BRICK)
    ),
    FROST_BOUND_BRICK_STAIRS(BlockDef.build("frostbound_brick_stairs", BlockBrickStairs.class)
            .block(() -> new BlockBrickStairs(BrickTheme.FROSTBOUND, BrickVariant.BRICK))
            .defaultItem()
            .condition(FROST_BOUND_BRICK)
    ),
    FROST_BOUND_PAVER_STAIRS(BlockDef.build("frostbound_paver_stairs", BlockBrickStairs.class)
            .block(() -> new BlockBrickStairs(BrickTheme.FROSTBOUND, BrickVariant.PAVER))
            .defaultItem()
            .condition(FROST_BOUND_BRICK)
    ),
    INFERNAL_BRICK_STAIRS(BlockDef.build("infernal_brick_stairs", BlockBrickStairs.class)
            .block(() -> new BlockBrickStairs(BrickTheme.INFERNAL, BrickVariant.BRICK))
            .defaultItem()
            .condition(INFERNAL_BRICK)
    ),
    INFERNAL_PAVER_STAIRS(BlockDef.build("infernal_paver_stairs", BlockBrickStairs.class)
            .block(() -> new BlockBrickStairs(BrickTheme.INFERNAL, BrickVariant.PAVER))
            .defaultItem()
            .condition(INFERNAL_BRICK)
    ),
    PEARLIZED_BRICK_STAIRS(BlockDef.build("pearlized_brick_stairs", BlockBrickStairs.class)
            .block(() -> new BlockBrickStairs(BrickTheme.PEARLIZED, BrickVariant.BRICK))
            .defaultItem()
            .condition(PEARLIZED_BRICK)
    ),
    PEARLIZED_PAVER_STAIRS(BlockDef.build("pearlized_paver_stairs", BlockBrickStairs.class)
            .block(() -> new BlockBrickStairs(BrickTheme.PEARLIZED, BrickVariant.PAVER))
            .defaultItem()
            .condition(PEARLIZED_BRICK)
    ),
    QUARRIED_BRICK_STAIRS(BlockDef.build("quarried_brick_stairs", BlockBrickStairs.class)
            .block(() -> new BlockBrickStairs(BrickTheme.QUARRIED, BrickVariant.BRICK))
            .defaultItem()
            .condition(QUARRIED_STONE)
            .condition(QUARRIED_BRICK)
    ),
    QUARRIED_PAVER_STAIRS(BlockDef.build("quarried_paver_stairs", BlockBrickStairs.class)
            .block(() -> new BlockBrickStairs(BrickTheme.QUARRIED, BrickVariant.PAVER))
            .defaultItem()
            .condition(QUARRIED_STONE)
            .condition(QUARRIED_BRICK)
    ),
    SANDY_BRICK_STAIRS(BlockDef.build("sandy_brick_stairs", BlockBrickStairs.class)
            .block(() -> new BlockBrickStairs(BrickTheme.SANDY, BrickVariant.BRICK))
            .defaultItem()
            .condition(SANDY_BRICK)
    ),
    SANDY_PAVER_STAIRS(BlockDef.build("sandy_paver_stairs", BlockBrickStairs.class)
            .block(() -> new BlockBrickStairs(BrickTheme.SANDY, BrickVariant.PAVER))
            .defaultItem()
            .condition(SANDY_BRICK)
    ),

    CHARGE_FEEDER(BlockDef.build("charge_feeder", BlockChargeFeeder.class).item(ItemMachine::new)),
    CHARGE_TRAP(BlockDef.build("charge_trap", BlockChargeTrap.class).defaultItem()),
    DETECTOR(BlockDef.build("detector", BlockDetector.class).item(ItemDetector::new)),
    EQUIPMENT(BlockDef.build("equipment", BlockMachineEquipment.class).item(ItemMachine::new)),
    FRAME(BlockDef.build("frame", BlockFrame.class).defaultItem()),
    GENERIC(BlockDef.build("generic", BlockGeneric.class).item(ItemBlockGeneric::new)),
    METAL(BlockDef.build("metal", BlockMetal.class).item(ItemBlockMetal::new)),
    GLASS(BlockDef.build("glass", BlockStrengthGlass.class).item(ItemBlockRailcraftColored::new)),
    LANTERN(BlockDef.build("lantern", BlockLantern.class).item(ItemMaterial::new)),
    LOGBOOK(BlockDef.build("logbook", BlockLogbook.class).defaultItem()),
    MANIPULATOR(BlockDef.build("manipulator", BlockMachineManipulator.class).item(ItemMachine::new)),
    ORE(BlockDef.build("ore", BlockOre.class).defaultSubtypedItem()),
    ORE_MAGIC(BlockDef.build("ore_magic", BlockOreMagic.class).item(ItemOreMagic::new)),
    ORE_METAL(BlockDef.build("ore_metal", BlockOreMetal.class).defaultSubtypedItem()),
    ORE_METAL_POOR(BlockDef.build("ore_metal_poor", BlockOreMetalPoor.class).defaultSubtypedItem()),
    POST(BlockDef.build("post", BlockPost.class).item(ItemPost::new)),
    POST_METAL(BlockDef.build("post_metal", BlockPostMetal.class).item(ItemPostMetal::new)),
    POST_METAL_PLATFORM(BlockDef.build("post_metal_platform", BlockPostMetalPlatform.class).item(ItemPostMetal::new)),
    REINFORCED_CONCRETE(BlockDef.build("reinforced_concrete", BlockReinforcedConcrete.class).item(ItemBlockRailcraftColored::new)),
    RITUAL(BlockDef.build("ritual", BlockRitual.class)),
    SIGNAL(BlockDef.build("signal", BlockMachineSignalRailcraft.class).item(ItemSignal::new)),
    SIGNAL_DUAL(BlockDef.build("signal_dual", BlockMachineSignalDualRailcraft.class).item(ItemSignal::new)),
    SIGNAL_BOX(BlockDef.build("signal_box", BlockMachineSignalBoxRailcraft.class).item(ItemMachine::new)),
    TRACK_ELEVATOR(BlockDef.build("track_elevator", BlockTrackElevator.class).item(ItemTrack::new)),

    // Flex Tracks

    TRACK_FLEX_ABANDONED(BlockDef.build("track_flex_abandoned", BlockTrackFlexAbandoned.class)
            .block(() -> new BlockTrackFlexAbandoned(TrackTypes.ABANDONED.getTrackType()))
            .item(ItemTrackStateless::new)),
    TRACK_FLEX_ELECTRIC(BlockDef.build("track_flex_electric", BlockTrackFlexElectric.class)
            .block(() -> new BlockTrackFlexElectric(TrackTypes.ELECTRIC.getTrackType()))
            .item(ItemTrackStateless::new)),
    TRACK_FLEX_HIGH_SPEED(BlockDef.build("track_flex_high_speed", BlockTrackFlex.class)
            .block(() -> new BlockTrackFlex(TrackTypes.HIGH_SPEED.getTrackType()))
            .item(ItemTrackStateless::new)),
    TRACK_FLEX_HS_ELECTRIC(BlockDef.build("track_flex_hs_electric", BlockTrackFlexElectric.class)
            .block(() -> new BlockTrackFlexElectric(TrackTypes.HIGH_SPEED_ELECTRIC.getTrackType()))
            .item(ItemTrackStateless::new)),
    TRACK_FLEX_REINFORCED(BlockDef.build("track_flex_reinforced", BlockTrackFlex.class)
            .block(() -> new BlockTrackFlex(TrackTypes.REINFORCED.getTrackType()))
            .item(ItemTrackStateless::new)),
    TRACK_FLEX_STRAP_IRON(BlockDef.build("track_flex_strap_iron", BlockTrackFlex.class)
            .block(() -> new BlockTrackFlex(TrackTypes.STRAP_IRON.getTrackType()))
            .item(ItemTrackStateless::new)),

    TRACK_FORCE(BlockDef.build("track_force", BlockTrackForce.class).item(ItemTrack::new)),
    TRACK_OUTFITTED(BlockDef.build("track_outfitted", BlockTrackOutfitted.class).item(ItemTrackOutfitted::new)),
    WIRE(BlockDef.build("wire", BlockWire.class).item(ItemWire::new)),
    WORLD_LOGIC(BlockDef.build("worldlogic", BlockWorldLogic.class).defaultItem()),
    WORLDSPIKE(BlockDef.build("worldspike", BlockWorldspike.class).item(ItemWorldspike::new)),
    WORLDSPIKE_POINT(BlockDef.build("worldspike_point", BlockWorldspikePoint.class).defaultItem()),
    // singles
    TRADE_STATION(BlockDef.build("trade_station", BlockTradeStation.class).defaultItem()),
    FORCE_TRACK_EMITTER(BlockDef.build("force_track_emitter", BlockForceTrackEmitter.class).defaultItem()),
    ADMIN_STEAM_PRODUCER(BlockDef.build("admin_steam_producer", BlockAdminSteamProducer.class).defaultItem()),
    CHEST_METALS(BlockDef.build("chest_metals", BlockChestMetals.class).defaultItem()),
    CHEST_VOID(BlockDef.build("chest_void", BlockChestVoid.class).defaultItem()),
    ENGINE_HOBBY(BlockDef.build("engine_steam_hobby", BlockEngine.class).block(() -> new BlockEngine("steam_hobby", TileEngineSteamHobby.class)).defaultItem()),
    ENGINE_LOW(BlockDef.build("engine_steam_low", BlockEngine.class).block(() -> new BlockEngine("steam_low", TileEngineSteamLow.class)).defaultItem()),
    ENGINE_HIGH(BlockDef.build("engine_steam_high", BlockEngine.class).block(() -> new BlockEngine("steam_high", TileEngineSteamHigh.class)).defaultItem()),
    // multiblocks
    COKE_OVEN(BlockDef.build("coke_oven", BlockCokeOvenSandy.class).defaultItem()),
    COKE_OVEN_RED(BlockDef.build("coke_oven_red", BlockCokeOvenRed.class).defaultItem()),
    BLAST_FURNACE(BlockDef.build("blast_furnace", BlockBlastFurnace.class).defaultItem()),
    ROCK_CRUSHER(BlockDef.build("rock_crusher", BlockRockCrusher.class).defaultItem()),
    STEAM_OVEN(BlockDef.build("steam_oven", BlockSteamOven.class).defaultItem()),
    TANK_IRON_GAUGE(BlockDef.build("tank_iron_gauge", GlassReplacer.class)),
    TANK_IRON_VALVE(BlockDef.build("tank_iron_valve", BlockTankIronValve.class).item(ItemBlockMetalTank::new)),
    TANK_IRON_WALL(BlockDef.build("tank_iron_wall", BlockTankIronWall.class).item(ItemBlockMetalTank::new)),
    TANK_STEEL_GAUGE(BlockDef.build("tank_steel_gauge", GlassReplacer.class)),
    TANK_STEEL_VALVE(BlockDef.build("tank_steel_valve", BlockTankSteelValve.class).item(ItemBlockMetalTank::new)),
    TANK_STEEL_WALL(BlockDef.build("tank_steel_wall", BlockTankSteelWall.class).item(ItemBlockMetalTank::new)),
    TANK_WATER(BlockDef.build("tank_water", BlockTankWater.class).defaultItem()),
    FLUX_TRANSFORMER(BlockDef.build("flux_transformer", BlockFluxTransformer.class).defaultItem()),
    BOILER_FIREBOX_FLUID(BlockDef.build("boiler_firebox_fluid", BlockBoilerFireboxFluid.class).defaultItem()),
    BOILER_FIREBOX_SOLID(BlockDef.build("boiler_firebox_solid", BlockBoilerFireboxSolid.class).defaultItem()),
    BOILER_TANK_PRESSURE_HIGH(BlockDef.build("boiler_tank_pressure_high", BlockBoilerTankHigh.class).defaultItem()),
    BOILER_TANK_PRESSURE_LOW(BlockDef.build("boiler_tank_pressure_low", BlockBoilerTankLow.class).defaultItem()),
    STEAM_TURBINE(BlockDef.build("steam_turbine", BlockSteamTurbine.class).defaultItem()),
    // others
    ;

    public static final RailcraftBlocks[] VALUES = values();

    public static <B extends Block & IRailcraftBlock> B byTag(String tag) {
        return (B) Arrays.stream(VALUES)
                .filter(bc -> StringUtils.equals(tag, bc.getRegistryName().getPath()))
                .findFirst()
                .flatMap(c -> c.def.block())
                .orElseThrow(() -> new IllegalArgumentException(tag + " not a valid block"));
    }

    private final BlockDef<? extends IRailcraftBlock, ? extends IRailcraftItemBlock> def;
    private final Class<? extends IVariantEnum> variantClass;

    <B extends Block & IRailcraftBlock, I extends ItemBlock & IRailcraftItemBlock>
    RailcraftBlocks(BlockDef<B, I> def) {
        this.def = def;
        def.obj = this;
        BlockMeta.Variant annotation = def.blockClass.getAnnotation(BlockMeta.Variant.class);
        this.variantClass = annotation != null ? annotation.value() : null;
        conditions().add(RailcraftConfig::isBlockEnabled, () -> "disabled via config");
    }

    private static class BlockDef<B extends Block & IRailcraftBlock, I extends ItemBlock & IRailcraftItemBlock> extends Definition<BlockDef<B, I>> {
        private IRailcraftObjectContainer<?> obj;
        private final Class<B> blockClass;
        private Supplier<B> blockSupplier = defaultSupplier();
        private @Nullable Function<B, I> itemSupplier;
        private Optional<B> block = Optional.empty();
        private Optional<I> item = Optional.empty();

        private BlockDef(String tag, Class<B> blockClass) {
            super(tag);
            this.blockClass = blockClass;
        }

        private static <B extends Block & IRailcraftBlock, I extends ItemBlock & IRailcraftItemBlock> BlockDef<B, I>
        build(String tag, Class<B> blockClass) {
            return new BlockDef<>(tag, blockClass);
        }

        private BlockDef<B, I> block(Supplier<B> blockSupplier) {
            this.blockSupplier = blockSupplier;
            return this;
        }

        private BlockDef<B, I> item(Function<B, I> itemSupplier) {
            this.itemSupplier = itemSupplier;
            return this;
        }

        @SuppressWarnings("unchecked")
        private BlockDef<B, I> defaultItem() {
            this.itemSupplier = b -> (I) new ItemBlockRailcraft<>(b);
            return this;
        }

        @SuppressWarnings("unchecked")
        private BlockDef<B, I> defaultSubtypedItem() {
            this.itemSupplier = b -> (I) new ItemBlockRailcraftSubtyped<>(b);
            return this;
        }

        private Supplier<B> defaultSupplier() {
            return () -> {
                try {
                    return blockClass.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            };
        }

        private void register() {
            if (block.isPresent())
                return;

            if (obj.isEnabled()) {
                block = Optional.of(blockSupplier.get());
                B b = block.get();
                b.setRegistryName(registryName);
                b.setTranslationKey("railcraft." + tag.replace("_", "."));

                I i = null;
                if (itemSupplier != null) {
                    item = Optional.of(itemSupplier.apply(b));
                    i = item.get();
                    i.setRegistryName(registryName);
                }

                RailcraftRegistry.register(b, i);

                b.initializeDefinition();

                if (i != null) {
                    if (i instanceof IRailcraftItemSimple)
                        throw new RuntimeException("Railcraft ItemBlocks must not implement IRailcraftItemSimple");
                    i.initializeDefinition();
                }
            } else {
                conditions.printFailureReason(obj);
            }
        }

        private Optional<B> block() {
            return block;
        }

        private Optional<I> item() {
            return item;
        }
    }

    @Override
    public Definition getDef() {
        return def;
    }

    @Override
    public void register() {
        def.register();
    }

    @SuppressWarnings("Convert2MethodRef")
    @Override
    public void defineRecipes() {
        def.block().ifPresent(o -> o.defineRecipes());
        def.item().ifPresent(o -> o.defineRecipes());
    }

    @SuppressWarnings("Convert2MethodRef")
    @Override
    public void finalizeDefinition() {
        def.block().ifPresent(o -> o.finalizeDefinition());
        def.item().ifPresent(o -> o.finalizeDefinition());
    }

    @SuppressWarnings("unused")
    public boolean isEqual(IVariantEnum variant, @Nullable ItemStack stack) {
        return !InvTools.isEmpty(stack) && def.block().isPresent() && InvTools.isItemEqual(stack, getStack(variant));
    }

    @SuppressWarnings("unused")
    public boolean isEqual(@Nullable Block block) {
        return def.block().isPresent() && def.block().get() == block;
    }

    public boolean isEqual(IBlockState state) {
        return def.block().isPresent() && def.block().get() == state.getBlock();
    }

    @Override
    public IBlockState getState(@Nullable IVariantEnum variant) {
        return def.block().map(b -> b.getState(variant)).orElseGet(this::getDefaultState);
    }

    public @Nullable <B extends Block & IRailcraftBlock> B getBlock() {
        return (B) def.block().orElse(null);
    }

    @Override
    public @Nullable ItemBlock item() {
        return def.item().orElse(null);
    }

    public @Nullable Class<? extends IVariantEnum> getVariantClass() {
        return variantClass;
    }

    @Override
    public Optional<IRailcraftBlock> getObject() {
        //noinspection unchecked
        return (Optional<IRailcraftBlock>) def.block();
    }

    @Override
    public boolean isLoaded() {
        return def.block().isPresent();
    }

    @Override
    public String toString() {
        return "Block{" + getBaseTag() + "}";
    }

}
