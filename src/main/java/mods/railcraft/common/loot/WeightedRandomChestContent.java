/*
 * Copyright (c) CovertJaguar, 2011-2017
 * http://railcraft.info
 *
 * This code is the property of CovertJaguar
 * and may only be used with explicit written
 * permission unless otherwise specified on the
 * license page at http://railcraft.info/wiki/info:license.
 */
package mods.railcraft.common.loot;

import net.minecraft.item.ItemStack;

public class WeightedRandomChestContent {

    private final ItemStack stack;
    private final int minStack;
    private final int maxStack;
    private final int lootChance;

    public WeightedRandomChestContent(ItemStack stack, int minStack, int maxStack, int lootChance) {
        this.stack = stack;
        this.minStack = minStack;
        this.maxStack = maxStack;
        this.lootChance = lootChance;
    }

    public ItemStack getStack() {
        return stack;
    }

    public int getMinStack() {
        return minStack;
    }

    public int getMaxStack() {
        return maxStack;
    }

    public int getLootChance() {
        return lootChance;
    }
}
