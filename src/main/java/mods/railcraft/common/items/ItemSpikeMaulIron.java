/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2017
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.items;

import mods.railcraft.common.plugins.forge.CraftingPlugin;
import net.minecraft.item.ItemStack;

/**
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class ItemSpikeMaulIron extends ItemSpikeMaul {

    public ItemSpikeMaulIron() {
        super(ItemMaterials.Material.IRON, ToolMaterial.IRON);
    }

    @Override
    public void defineRecipes() {
        CraftingPlugin.addRecipe(new ItemStack(this),
                "IBI",
                " S ",
                " S ",
                'I', "ingotIron",
                'B', "blockIron",
                'S', "stickWood");
    }
}
