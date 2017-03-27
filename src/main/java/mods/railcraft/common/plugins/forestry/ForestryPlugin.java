/*
 * Copyright (c) CovertJaguar, 2011-2017
 * http://railcraft.info
 *
 * This code is the property of CovertJaguar
 * and may only be used with explicit written
 * permission unless otherwise specified on the
 * license page at http://railcraft.info/wiki/info:license.
 */
package mods.railcraft.common.plugins.forestry;

import forestry.api.storage.EnumBackpackType;
import mods.railcraft.common.core.RailcraftConfig;
import mods.railcraft.common.fluids.Fluids;
import mods.railcraft.common.plugins.forge.CraftingPlugin;
import mods.railcraft.common.plugins.forge.CreativePlugin;
import mods.railcraft.common.plugins.forge.LocalizationPlugin;
import mods.railcraft.common.plugins.forge.RailcraftRegistry;
import mods.railcraft.common.plugins.misc.Mod;
import mods.railcraft.common.util.crafting.InvalidRecipeException;
import mods.railcraft.common.util.inventory.InvTools;
import mods.railcraft.common.util.misc.Game;
import mods.railcraft.common.util.misc.MiscTools;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;

/**
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class ForestryPlugin {
    public static final String FORESTRY_ID = "forestry";
    public static final String FORESTRY_NAME = "Forestry";
    public static Item trackmanBackpackT1;
    public static Item trackmanBackpackT2;
    public static Item icemanBackpackT1;
    public static Item icemanBackpackT2;
    public static Item apothecariesBackpackT1;
    public static Item apothecariesBackpackT2;
    private static ForestryPlugin instance;

    public static ForestryPlugin instance() {
        if (instance == null) {
            if (Mod.FORESTRY.isLoaded())
                instance = new ForestryPluginInstalled();
            else
                instance = new ForestryPlugin();
        }
        return instance;
    }

    public static ItemStack getItem(String tag) {
        if (!Mod.FORESTRY.isLoaded())
            return null;
        Item item = GameRegistry.findItem("forestry", tag);
        if (item == null)
            return null;
        return new ItemStack(item, 1);
    }

    public static void addBackpackItem(String pack, @Nullable ItemStack stack) {
        if (stack == null)
            return;
        addBackpackItem(pack, stack.getItem(), stack.getItemDamage());
    }

    public static void addBackpackItem(String pack, Item item) {
        sendBackpackMessage(String.format("%s@%s:*", pack, ForgeRegistries.ITEMS.getKey(item)));
    }

    public static void addBackpackItem(String pack, Item item, int damage) {
        sendBackpackMessage(String.format("%s@%s:%d", pack, ForgeRegistries.ITEMS.getKey(item), damage));
    }

    public static void addBackpackItem(String pack, Block block) {
        sendBackpackMessage(String.format("%s@%s:*", pack, ForgeRegistries.BLOCKS.getKey(block)));
    }

    public static void addBackpackItem(String pack, Block block, int meta) {
        sendBackpackMessage(String.format("%s@%s:%d", pack, ForgeRegistries.BLOCKS.getKey(block), meta));
    }

    private static void sendBackpackMessage(String message) {
        if (message.contains("null"))
            throw new IllegalArgumentException("Attempting to register broken item with Forestry Backpack!");
//        Game.logDebug(Level.FINEST, "Sending IMC to Forestry add-backpack-items: {0}", message);
        FMLInterModComms.sendMessage(ForestryPlugin.FORESTRY_ID, "add-backpack-items", message);
    }

    public void registerBackpacks() {
    }

    public void setupBackpackContents() {
    }

    public void addCarpenterRecipe(String recipeTag,
                                   int packagingTime,
                                   FluidStack liquid,
                                   @Nullable ItemStack box,
                                   @Nullable ItemStack product,
                                   Object... materials) {
    }

    private static class ForestryPluginInstalled extends ForestryPlugin {
        @Override
        @Optional.Method(modid = ForestryPlugin.FORESTRY_ID)
        public void registerBackpacks() {
            try {
                if (forestry.api.storage.BackpackManager.backpackInterface == null)
                    return;

                String tag = "railcraft.backpack.trackman.t1";
                if (RailcraftConfig.isItemEnabled(tag)) {
                    trackmanBackpackT1 = registerBackpack(TrackmanBackpack.getInstance(), EnumBackpackType.NORMAL, tag);

                    ItemStack output = new ItemStack(trackmanBackpackT1);
                    addBackpackTooltip(output);
                    CraftingPlugin.addRecipe(output,
                            "X#X",
                            "VYV",
                            "X#X",
                            '#', Blocks.WOOL,
                            'V', new ItemStack(Blocks.RAIL),
                            'X', Items.STRING,
                            'Y', "chestWood");
                }

                tag = "railcraft.backpack.trackman.t2";
                if (RailcraftConfig.isItemEnabled(tag)) {
                    trackmanBackpackT2 = registerBackpack(TrackmanBackpack.getInstance(), EnumBackpackType.WOVEN, tag);

                    ItemStack silk = getItem("craftingMaterial");

                    if (silk != null) {
                        silk.setItemDamage(3);

                        ItemStack output = new ItemStack(trackmanBackpackT2);
                        addBackpackTooltip(output);
                        forestry.api.recipes.RecipeManagers.carpenterManager.addRecipe(200, Fluids.WATER.get(1000), null, output, "WXW",
                                "WTW",
                                "WWW",
                                'X', "gemDiamond",
                                'W', silk,
                                'T', trackmanBackpackT1);
                    }
                }

                tag = "railcraft.backpack.iceman.t1";
                if (RailcraftConfig.isItemEnabled(tag)) {
                    icemanBackpackT1 = registerBackpack(IcemanBackpack.getInstance(), EnumBackpackType.NORMAL, tag);

                    ItemStack output = new ItemStack(icemanBackpackT1);
                    addBackpackTooltip(output);
                    CraftingPlugin.addRecipe(output,
                            "X#X",
                            "VYV",
                            "X#X",
                            '#', Blocks.WOOL,
                            'V', new ItemStack(Blocks.SNOW),
                            'X', Items.STRING,
                            'Y', "chestWood");
                }

                tag = "railcraft.backpack.iceman.t2";
                if (RailcraftConfig.isItemEnabled(tag)) {
                    icemanBackpackT2 = registerBackpack(IcemanBackpack.getInstance(), EnumBackpackType.WOVEN, tag);

                    ItemStack silk = getItem("craftingMaterial");

                    if (silk != null) {
                        silk.setItemDamage(3);

                        ItemStack output = new ItemStack(icemanBackpackT2);
                        addBackpackTooltip(output);
                        forestry.api.recipes.RecipeManagers.carpenterManager.addRecipe(200, Fluids.WATER.get(1000), null, output, "WXW",
                                "WTW",
                                "WWW",
                                'X', "gemDiamond",
                                'W', silk,
                                'T', icemanBackpackT1);
                    }
                }

                if (icemanBackpackT1 != null || icemanBackpackT2 != null)
                    MinecraftForge.EVENT_BUS.register(new IceManTickHandler());

                tag = "railcraft.backpack.apothecary.t1";
                if (RailcraftConfig.isItemEnabled(tag)) {
                    apothecariesBackpackT1 = registerBackpack(ApothecariesBackpack.getInstance(), EnumBackpackType.NORMAL, tag);

                    ItemStack output = new ItemStack(apothecariesBackpackT1);
                    addBackpackTooltip(output);
//                if (!ThaumcraftPlugin.isModInstalled()) {
                    CraftingPlugin.addRecipe(output,
                            "X#X",
                            "VYV",
                            "X#X",
                            '#', Blocks.WOOL,
                            'V', new ItemStack(Items.POTIONITEM, 1, 8197),
                            'X', Items.STRING,
                            'Y', "chestWood");
                    CraftingPlugin.addRecipe(output,
                            "X#X",
                            "VYV",
                            "X#X",
                            '#', Blocks.WOOL,
                            'V', new ItemStack(Items.POTIONITEM, 1, 8261),
                            'X', Items.STRING,
                            'Y', "chestWood");
                    CraftingPlugin.addRecipe(output,
                            "X#X",
                            "VYV",
                            "X#X",
                            '#', Blocks.WOOL,
                            'V', new ItemStack(Items.POTIONITEM, 1, 8229),
                            'X', Items.STRING,
                            'Y', "chestWood");
//                } else
//                    ApothecariesBackpack.registerThaumcraftResearch();
                }

                tag = "railcraft.backpack.apothecary.t2";
                if (RailcraftConfig.isItemEnabled(tag)) {
                    apothecariesBackpackT2 = registerBackpack(ApothecariesBackpack.getInstance(), EnumBackpackType.WOVEN, tag);

                    ItemStack silk = getItem("craftingMaterial");

                    if (silk != null) {
                        silk.setItemDamage(3);

                        ItemStack output = new ItemStack(apothecariesBackpackT2);
                        addBackpackTooltip(output);
                        forestry.api.recipes.RecipeManagers.carpenterManager.addRecipe(200, Fluids.WATER.get(1000), null, output, "WXW",
                                "WTW",
                                "WWW",
                                'X', "gemDiamond",
                                'W', silk,
                                'T', apothecariesBackpackT1);
                    }
                }
            } catch (Throwable error) {
                Game.logErrorAPI(ForestryPlugin.FORESTRY_NAME, error, forestry.api.storage.BackpackManager.class);
            }
        }

        @Optional.Method(modid = ForestryPlugin.FORESTRY_ID)
        private Item registerBackpack(BaseBackpack backpack, forestry.api.storage.EnumBackpackType type, String tag) {
            forestry.api.storage.BackpackManager.backpackInterface.registerBackpackDefinition(backpack.getId(), backpack);
            Item item = forestry.api.storage.BackpackManager.backpackInterface.createBackpack(backpack.getId(), type).setCreativeTab(CreativePlugin.RAILCRAFT_TAB).setUnlocalizedName(tag);
            item.setRegistryName(MiscTools.cleanTag(tag));
            RailcraftRegistry.register(item);
            return item;
        }

        @Optional.Method(modid = ForestryPlugin.FORESTRY_ID)
        private void addBackpackTooltip(ItemStack stack) {
            InvTools.addItemToolTip(stack, "\u00a77\u00a7o" + LocalizationPlugin.translate("item.railcraft.backpack.tips"));
        }

        @Override
        @Optional.Method(modid = ForestryPlugin.FORESTRY_ID)
        public void setupBackpackContents() {
            try {
                if (forestry.api.storage.BackpackManager.backpackInterface == null)
                    return;
                TrackmanBackpack.getInstance().setup();
                IcemanBackpack.getInstance().setup();
                ApothecariesBackpack.getInstance().setup();
            } catch (Throwable error) {
                Game.logErrorAPI(ForestryPlugin.FORESTRY_NAME, error, forestry.api.storage.BackpackManager.class);
            }
        }

        @Override
        @Optional.Method(modid = ForestryPlugin.FORESTRY_ID)
        public void addCarpenterRecipe(String recipeTag, int packagingTime, FluidStack liquid, @Nullable ItemStack box, @Nullable ItemStack product, Object... materials) {
            if (product == null) {
                Game.logTrace(Level.WARN, "Tried to define invalid Carpenter recipe \"{0}\", the result was null or zero. Skipping", recipeTag);
                return;
            }
            try {
                materials = CraftingPlugin.cleanRecipeArray(CraftingPlugin.RecipeType.SHAPED, product, materials);
            } catch (InvalidRecipeException ex) {
                Game.logTrace(Level.WARN, "Tried to define invalid Carpenter recipe \"{0}\", some ingredients were missing. Skipping", recipeTag);
                Game.logTrace(Level.WARN, ex.getRawMessage());
                return;
            }
            try {
                if (forestry.api.recipes.RecipeManagers.carpenterManager != null && RailcraftConfig.getRecipeConfig("forestry.carpenter." + recipeTag))
                    forestry.api.recipes.RecipeManagers.carpenterManager.addRecipe(packagingTime, liquid, box, product, materials);
            } catch (Throwable error) {
                Game.logErrorAPI(ForestryPlugin.FORESTRY_NAME, error, forestry.api.recipes.RecipeManagers.class);
            }
        }
    }
}
