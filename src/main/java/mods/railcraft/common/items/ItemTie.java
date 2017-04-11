/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2017
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.items;

import mods.railcraft.api.core.IRailcraftRecipeIngredient;
import mods.railcraft.api.core.IVariantEnum;
import mods.railcraft.common.fluids.FluidTools;
import mods.railcraft.common.fluids.Fluids;
import mods.railcraft.common.plugins.forge.CraftingPlugin;
import mods.railcraft.common.plugins.forge.RailcraftRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.Locale;

public class ItemTie extends ItemRailcraftSubtyped {

    public ItemTie() {
        super(EnumTie.class);
    }

    @Override
    public void initializeDefinintion() {
        for (EnumTie tie : EnumTie.VALUES) {
            RailcraftRegistry.register(this, tie, new ItemStack(this, 1, tie.ordinal()));
        }
    }

    @Override
    public void defineRecipes() {
        ItemStack tieStone = RailcraftItems.TIE.getStack(1, EnumTie.STONE);
        CraftingPlugin.addRecipe(tieStone,
                " O ",
                "#r#",
                'O', Items.WATER_BUCKET,
                'r', RailcraftItems.REBAR,
                '#', RailcraftItems.CONCRETE);
    }

    @Override
    public void finalizeDefinition() {
        ItemStack tieWood = RailcraftItems.TIE.getStack(1, EnumTie.WOOD);
        for (ItemStack container : FluidTools.getContainersFilledWith(Fluids.CREOSOTE.getB(1))) {
            CraftingPlugin.addRecipe(tieWood,
                    " O ",
                    "###",
                    'O', container,
                    '#', "slabWood");
        }
    }

    public enum EnumTie implements IVariantEnum {
        WOOD("slabWood"),
        STONE(Blocks.STONE_SLAB);
        public static final EnumTie[] VALUES = values();
        private Object alternate;

        EnumTie(Object alt) {
            this.alternate = alt;
        }

        @Override
        public Object getAlternate(IRailcraftRecipeIngredient container) {
            return alternate;
        }

        @Override
        public String getName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

}
