/*
 * Copyright (c) CovertJaguar, 2011-2017
 * http://railcraft.info
 *
 * This code is the property of CovertJaguar
 * and may only be used with explicit written
 * permission unless otherwise specified on the
 * license page at http://railcraft.info/wiki/info:license.
 */
package mods.railcraft.common.blocks.aesthetics.generic;

import mods.railcraft.common.blocks.ItemBlockRailcraft;
import mods.railcraft.common.items.ItemCoke;
import mods.railcraft.common.plugins.forge.LocalizationPlugin;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.List;

public class ItemBlockGeneric extends ItemBlockRailcraft {

    public ItemBlockGeneric(Block block) {
        super(block);
        setMaxDamage(0);
        setHasSubtypes(true);
    }

    @Override
    public int getMetadata(int meta) {
        return meta;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return EnumGeneric.fromOrdinal(stack.getItemDamage()).getTag();
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> info, boolean adv) {
        super.addInformation(stack, player, info, adv);
        String tag = getUnlocalizedName(stack) + ".tips";
        if (LocalizationPlugin.hasTag(tag))
            info.add(LocalizationPlugin.translate(tag));
    }

    @Override
    public int getHeatValue(ItemStack stack) {
        int meta = stack.getMetadata();
        if (meta == EnumGeneric.BLOCK_COKE.ordinal())
            return ItemCoke.COKE_HEAT * 10;
        if (meta == EnumGeneric.BLOCK_CREOSOTE.ordinal())
            return 600;
        return 0;
    }
}
