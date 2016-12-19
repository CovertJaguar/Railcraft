/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2016
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.util.crafting;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import mods.railcraft.api.crafting.IBlastFurnaceCraftingManager;
import mods.railcraft.api.crafting.IBlastFurnaceRecipe;
import mods.railcraft.api.crafting.RailcraftCraftingManager;
import mods.railcraft.common.blocks.aesthetics.generic.EnumGeneric;
import mods.railcraft.common.items.RailcraftItems;
import mods.railcraft.common.util.inventory.InvTools;

public class BlastFurnaceCraftingManager implements IBlastFurnaceCraftingManager {

    private final List<BlastFurnaceRecipe> recipes = new ArrayList<BlastFurnaceRecipe>();
    private List<ItemStack> fuels;

    public static IBlastFurnaceCraftingManager getInstance() {
        return RailcraftCraftingManager.blastFurnace;
    }

    @Override
    public List<ItemStack> getFuels() {
        if (fuels == null) {
            List<ItemStack> fuel = new ArrayList<ItemStack>() {
                @Override
                public boolean add(ItemStack e) {
                    return e != null && super.add(e);
                }

            };
//            fuel.add(ThaumcraftPlugin.ITEMS.get("alumentum", 0));
            fuel.add(RailcraftItems.COKE.getStack());
            fuel.add(EnumGeneric.BLOCK_COKE.getStack());
            fuel.add(new ItemStack(Items.COAL, 1, 1));
            fuel.add(RailcraftItems.FIRESTONE_REFINED.getWildcard());
            fuel.add(RailcraftItems.FIRESTONE_CRACKED.getWildcard());
            fuels = Collections.unmodifiableList(fuel);
        }
        return fuels;
    }

    @Override
    public List<? extends IBlastFurnaceRecipe> getRecipes() {
        return recipes;
    }

    public static class BlastFurnaceRecipe implements IBlastFurnaceRecipe {

        private final ItemStack input;
        private final boolean matchDamage;
        private final boolean matchNBT;
        private final int cookTime;
        private final ItemStack output;

        public BlastFurnaceRecipe(ItemStack input, boolean matchDamage, boolean matchNBT, int cookTime, ItemStack output) {
            this.input = input.copy();
            this.matchDamage = matchDamage;
            this.matchNBT = matchNBT;
            this.cookTime = cookTime;
            this.output = output.copy();
        }

        @Override
        public boolean isRoomForOutput(ItemStack outputSlot) {
            return (outputSlot == null || output == null || (InvTools.isItemEqual(outputSlot, output) && outputSlot.getCount() + output.getCount() <= output.getMaxStackSize()));
        }

        @Override
        public ItemStack getInput() {
            return input.copy();
        }

        public boolean matchDamage() {
            return matchDamage;
        }

        public boolean matchNBT() {
            return matchNBT;
        }

        @Override
        public ItemStack getOutput() {
            return output.copy();
        }

        @Override
        public int getOutputStackSize() {
            if (output == null)
                return 0;
            return output.getCount();
        }

        @Override
        public int getCookTime() {
            return cookTime;
        }

    }

    @Override
    public void addRecipe(@Nullable ItemStack input, boolean matchDamage, boolean matchNBT, int cookTime, @Nullable ItemStack output) {
        if (input != null && output != null)
            recipes.add(new BlastFurnaceRecipe(input, matchDamage, matchNBT, cookTime, output));
    }

    @Override
    public IBlastFurnaceRecipe getRecipe(ItemStack input) {
        if (input == null) return null;
        for (BlastFurnaceRecipe r : recipes) {
            if (!r.matchDamage || InvTools.isWildcard(r.input)) continue;
            if (InvTools.isItemEqual(input, r.input, true, r.matchNBT))
                return r;
        }
        for (BlastFurnaceRecipe r : recipes) {
            if (InvTools.isItemEqual(input, r.input, r.matchDamage, r.matchNBT))
                return r;
        }
        return null;
    }

}
