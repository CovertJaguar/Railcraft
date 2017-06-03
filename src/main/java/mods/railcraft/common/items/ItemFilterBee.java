/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2017
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/

package mods.railcraft.common.items;

import mods.railcraft.api.core.items.IFilterItem;
import mods.railcraft.common.plugins.forestry.ForestryPlugin;
import mods.railcraft.common.plugins.forge.CraftingPlugin;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Optional;

/**
 * Created by CovertJaguar on 5/29/2017 for Railcraft.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class ItemFilterBee extends ItemRailcraft implements IFilterItem {

    public ItemFilterBee() {
    }

    @Override
    public void finalizeDefinition() {
        CraftingPlugin.addShapelessRecipe(getStack(), RailcraftItems.FILTER_BLANK, ModItems.HONEY_DROP);
    }

    @Optional.Method(modid = ForestryPlugin.FORESTRY_ID)
    @Override
    public boolean matches(ItemStack matcher, ItemStack target) {
        return ForestryPlugin.instance().isBee(target);
    }
}
