/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2017
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.items.enchantment;

import net.minecraft.inventory.EntityEquipmentSlot;

/**
 * Boost enchantment.
 */
public class EnchantmentSmack extends EnchantmentToolRailcraft {

    public EnchantmentSmack(Rarity rarity) {
        super("smack", rarity, EntityEquipmentSlot.MAINHAND);
    }

    @Override
    public int getMinEnchantability(int level) {
        return 9 + level * 8;
    }

    @Override
    public int getMaxEnchantability(int level) {
        return getMinEnchantability(level) + 20;
    }

    @Override
    public int getMaxLevel() {
        return 4;
    }
}
