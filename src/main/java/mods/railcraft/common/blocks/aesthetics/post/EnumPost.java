/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2019
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.blocks.aesthetics.post;

import mods.railcraft.common.blocks.IRailcraftBlockContainer;
import mods.railcraft.common.blocks.IVariantEnumBlock;
import mods.railcraft.common.blocks.RailcraftBlocks;
import mods.railcraft.common.modules.ModuleStructures;
import net.minecraft.block.material.MapColor;

import java.util.Locale;

/**
 * @author CovertJaguar <http://www.railcraft.info>
 */
public enum EnumPost implements IVariantEnumBlock<EnumPost> {

    WOOD(MapColor.BROWN),
    STONE(MapColor.STONE),
    METAL_UNPAINTED(MapColor.NETHERRACK),
    EMBLEM(MapColor.IRON),
    WOOD_PLATFORM(MapColor.BROWN),
    STONE_PLATFORM(MapColor.STONE),
    METAL_PLATFORM_UNPAINTED(MapColor.NETHERRACK);

    static {
        WOOD.burnable = true;
        WOOD_PLATFORM.burnable = true;
    }

    public static final EnumPost[] VALUES = values();
    private final MapColor mapColor;
    private final Definition def;
    boolean burnable = false;

    EnumPost(MapColor mapColor) {
        this.mapColor = mapColor;
        this.def = new Definition(name().toLowerCase(Locale.ROOT), ModuleStructures.class);
    }

    public static EnumPost fromId(int id) {
        if (id < 0 || id >= EnumPost.values().length)
            id = 0;
        return EnumPost.values()[id];
    }

    @Override
    public Definition getDef() {
        return def;
    }

    public MapColor getMapColor() {
        return mapColor;
    }

    public boolean canBurn() {
        return burnable;
    }

    @Override
    public String getTag() {
        return "tile.railcraft.post." + getBaseTag();
    }

    @Override
    public IRailcraftBlockContainer getContainer() {
        return RailcraftBlocks.POST;
    }

}
