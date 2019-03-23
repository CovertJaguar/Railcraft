/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2019
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/

package mods.railcraft.common.blocks.aesthetics.metals;

import mods.railcraft.api.core.IRailcraftModule;
import mods.railcraft.common.blocks.IRailcraftBlockContainer;
import mods.railcraft.common.blocks.IVariantEnumBlock;
import mods.railcraft.common.blocks.RailcraftBlocks;
import mods.railcraft.common.blocks.aesthetics.generic.SimpleCube;
import mods.railcraft.common.modules.ModuleResources;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public enum EnumMetal implements IVariantEnumBlock<EnumMetal> {

    BLOCK_COPPER(ModuleResources.class, "copper", new MetalCube(), 3f, 10f),
    BLOCK_TIN(ModuleResources.class, "tin", new MetalCube(), 3f, 10f),
    BLOCK_LEAD(ModuleResources.class, "lead", new MetalCube(), 2f, 20f),
    BLOCK_STEEL(ModuleResources.class, "steel", new MetalCube(), 5f, 15f),
    BLOCK_SILVER(ModuleResources.class, "silver", new MetalCube(), 3f, 10f),
    BLOCK_BRONZE(ModuleResources.class, "bronze", new MetalCube(), 3f, 10f),
    BLOCK_NICKEL(ModuleResources.class, "nickel", new MetalCube(), 3f, 10f),
    BLOCK_INVAR(ModuleResources.class, "invar", new MetalCube(), 3f, 10f),
    BLOCK_ZINC(ModuleResources.class, "zinc", new MetalCube(), 3f, 10f),
    BLOCK_BRASS(ModuleResources.class, "brass", new MetalCube(), 3f, 10f),
    ;
    public static final EnumMetal[] VALUES = values();
    private static final List<EnumMetal> creativeList = new ArrayList<>();

    static {
        creativeList.add(BLOCK_COPPER);
        creativeList.add(BLOCK_TIN);
        creativeList.add(BLOCK_LEAD);
        creativeList.add(BLOCK_SILVER);
        creativeList.add(BLOCK_STEEL);
        creativeList.add(BLOCK_BRONZE);
        creativeList.add(BLOCK_NICKEL);
        creativeList.add(BLOCK_INVAR);
        creativeList.add(BLOCK_ZINC);
        creativeList.add(BLOCK_BRASS);
    }

    private final SimpleCube blockDef;
    private final float hardness;
    private final float resistance;
    private final Definition def;

    EnumMetal(@Nullable Class<? extends IRailcraftModule> module, String tag, SimpleCube blockDef, float hardness, float resistance) {
        this.blockDef = blockDef;
        this.hardness = hardness;
        this.resistance = resistance;
        this.def = new Definition(tag, module);
    }

    @Override
    public Definition getDef() {
        return def;
    }

    public static List<EnumMetal> getCreativeList() {
        return creativeList;
    }

    public static EnumMetal fromOrdinal(int id) {
        return VALUES[id];
    }

    @Override
    public String getTag() {
        return "tile.railcraft.metal_" + getBaseTag();
    }

    public SimpleCube getBlockDef() {
        return blockDef;
    }

    @Override
    public IRailcraftBlockContainer getContainer() {
        return RailcraftBlocks.METAL;
    }

    public float getHardness() {
        return hardness;
    }

    public float getResistance() {
        return resistance;
    }

}
