/*
 * Copyright (c) CovertJaguar, 2011-2017
 * http://railcraft.info
 *
 * This code is the property of CovertJaguar
 * and may only be used with explicit written
 * permission unless otherwise specified on the
 * license page at http://railcraft.info/wiki/info:license.
 */
package mods.railcraft.common.plugins.forge;

import mods.railcraft.api.core.IVariantEnum;
import mods.railcraft.common.core.IRailcraftObjectContainer;
import mods.railcraft.common.core.RailcraftConfig;
import mods.railcraft.common.core.RailcraftConstants;
import mods.railcraft.common.items.IRailcraftItemSimple;
import mods.railcraft.common.loot.WeightedRandomChestContent;
import mods.railcraft.common.util.misc.Game;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

import static net.minecraft.world.storage.loot.LootTableList.*;

/**
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class LootPlugin {

    public static final ResourceLocation CHESTS_VILLAGE_WORKSHOP = register(new ResourceLocation(RailcraftConstants.RESOURCE_DOMAIN, "chests/village_workshop"));

    public static void init() {
        /*LootPlugin.increaseLootGen(1, 2,
                CHESTS_ABANDONED_MINESHAFT,
                CHESTS_VILLAGE_BLACKSMITH);
        LootPlugin.increaseLootGen(10, 16, CHESTS_VILLAGE_WORKSHOP);*/
        addLoot(new ItemStack(Items.COAL), 8, 16, Type.WORKSHOP, "fuel_coal");
    }

    /*public static void increaseLootGen(int min, int max, ResourceLocation... locations) {
        for (ResourceLocation location : locations) {
            ChestGenHooks lootInfo = ChestGenHooks.getInfo(location);
            lootInfo.setMin(lootInfo.getMin() + min);
            lootInfo.setMax(lootInfo.getMax() + max);
        }
    }*/

    private static void addLoot(@Nullable ItemStack loot, int minStack, int maxStack, String tag, ResourceLocation... locations) {
        if (loot == null) {
            if (Game.DEVELOPMENT_ENVIRONMENT)
                throw new RuntimeException("Invalid Loot");
            return;
        }
        WeightedRandomChestContent contents = new WeightedRandomChestContent(loot, minStack, maxStack, RailcraftConfig.getLootChance(tag));
        addLoot(contents, locations);
    }

    private static void addLoot(WeightedRandomChestContent loot, ResourceLocation... locations) {
        for (ResourceLocation location : locations) {
            //TODO: this broken, fix
//            LootEventDispatcher.addItem(location, loot);
        }
    }

    public static void addLoot(ItemStack loot, int minStack, int maxStack, Type type, String tag) {
        addLoot(loot, minStack, maxStack, tag, type.locations);
    }

    public static void addLoot(ItemStack loot, int minStack, int maxStack, Type type) {
        addLoot(loot, minStack, maxStack, loot.getItem().getRegistryName().getResourcePath(), type.locations);
    }

    public static void addLoot(IRailcraftObjectContainer<IRailcraftItemSimple> itemContainer, IVariantEnum variant, int minStack, int maxStack, Type type) {
        addLoot(itemContainer.getStack(variant), minStack, maxStack, itemContainer.getBaseTag(), type.locations);
    }

    public static void addLootUnique(IRailcraftObjectContainer<IRailcraftItemSimple> itemContainer, IVariantEnum variant, int minStack, int maxStack, Type type) {
        addLoot(itemContainer.getStack(variant), minStack, maxStack, itemContainer.getBaseTag() + RailcraftConstants.SEPERATOR + variant.getResourcePathSuffix(), type.locations);
    }

    public static void addLoot(IRailcraftObjectContainer<IRailcraftItemSimple> item, int minStack, int maxStack, Type type) {
        addLoot(item.getStack(), minStack, maxStack, item.getBaseTag(), type.locations);
    }

    public enum Type {
        WARRIOR(CHESTS_VILLAGE_BLACKSMITH,
                CHESTS_SIMPLE_DUNGEON,
                CHESTS_DESERT_PYRAMID,
                CHESTS_JUNGLE_TEMPLE,
                CHESTS_STRONGHOLD_CORRIDOR,
                CHESTS_STRONGHOLD_CROSSING),

        RAILWAY(CHESTS_ABANDONED_MINESHAFT,
                CHESTS_VILLAGE_WORKSHOP),

        WORKSHOP(CHESTS_VILLAGE_WORKSHOP),

        TOOL(CHESTS_ABANDONED_MINESHAFT,
                CHESTS_VILLAGE_BLACKSMITH);

        private final ResourceLocation[] locations;

        Type(ResourceLocation... locations) {
            this.locations = locations;
        }
    }
}
