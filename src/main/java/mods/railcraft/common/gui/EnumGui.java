/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2017
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.gui;

/**
 * @author CovertJaguar
 */
public enum EnumGui {

    MANIPULATOR_ITEM(true),
    MANIPULATOR_FLUID(true),
    MANIPULATOR_RF(true),
    LOADER_ENERGY(true),
    UNLOADER_ENERGY(true),
    DETECTOR_CART(true),
    DETECTOR_ITEM(true),
    DETECTOR_TANK(true),
    DETECTOR_ANIMAL(false),
    DETECTOR_SHEEP(true),
    DETECTOR_ADVANCED(true),
    DETECTOR_TRAIN(false),
    DETECTOR_VILLAGER(false),
    DETECTOR_LOCOMOTIVE(true),
    DETECTOR_ROUTING(true),
    COKE_OVEN(true),
    STEAN_OVEN(true),
    BLAST_FURNACE(true),
    TANK(true),
    ROCK_CRUSHER(true),
    ROLLING_MACHINE_MANUAL(true),
    ROLLING_MACHINE_POWERED(true),
    ENGRAVING_BENCH(true),
    ENGRAVING_BENCH_UNLOCK(true),
    CART_DISPENSER(true),
    TRAIN_DISPENSER(true),
    FEED_STATION(true),
    TRADE_STATION(true),
    WORLDSPIKE(true),
    TRACK_PRIMING(false),
    TRACK_LAUNCHER(false),
    TRACK_EMBARKING(false),
    TRACK_ROUTING(true),
    CART_BORE(true),
    CART_TNT_FUSE(false),
    CART_TNT_BLAST(false),
    CART_CARGO(true),
    CART_WORK(true),
    CART_ENERGY(true),
    CART_RF(true),
    CART_TANK(true),
    CART_WORLDSPIKE(true),
    CART_TRACK_RELAYER(true),
    CART_UNDERCUTTER(true),
    CART_TRACK_LAYER(true),
    LOCO_STEAM(true),
    LOCO_ELECTRIC(true),
    LOCO_CREATIVE(true),
    BOX_CONTROLLER(false),
    BOX_ANALOG_CONTROLLER(false),
    BOX_RECEIVER(true),
    BOX_CAPACITOR(false),
    BOX_RELAY(true),
    ROUTING(true),
    SWITCH_MOTOR(true),
    ENGINE_HOBBY(true),
    ENGINE_STEAM(true),
    BOILER_SOLID(true),
    BOILER_LIQUID(true),
    TURBINE(true),
    ANVIL(true),
    TRACK_DUMPING(true);
    private static final EnumGui[] VALUES = values();
    private final boolean hasContainer;

    EnumGui(boolean hasContainer) {
        this.hasContainer = hasContainer;
    }

    public boolean hasContainer() {
        return hasContainer;
    }

    public static EnumGui fromOrdinal(int i) {
        if (i < 0 || i >= VALUES.length)
            return null;
        return VALUES[i];
    }

}
