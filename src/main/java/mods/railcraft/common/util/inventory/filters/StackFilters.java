/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2020
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.util.inventory.filters;

import mods.railcraft.api.carts.IMinecart;
import mods.railcraft.api.items.IMinecartItem;
import mods.railcraft.common.blocks.tracks.TrackTools;
import mods.railcraft.common.core.RailcraftConfig;
import mods.railcraft.common.fluids.FluidItemHelper;
import mods.railcraft.common.items.Metal;
import mods.railcraft.common.items.RailcraftItems;
import mods.railcraft.common.plugins.forge.FuelPlugin;
import mods.railcraft.common.plugins.forge.OreDictPlugin;
import mods.railcraft.common.util.inventory.IInventoryComposite;
import mods.railcraft.common.util.inventory.InvTools;
import mods.railcraft.common.util.misc.BallastRegistry;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.UniversalBucket;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A collection of helper methods for creating {@code Predicate<ItemStack>} objects.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public enum StackFilters implements Predicate<ItemStack> {

    ALL,
    FUEL {
        @Override
        protected boolean testType(ItemStack stack) {
            return FuelPlugin.getBurnTime(stack) > 0;
        }

    },
    TRACK {
        @Override
        protected boolean testType(ItemStack stack) {
            return TrackTools.isRail(stack);
        }

    },
    MINECART {
        @Override
        protected boolean testType(ItemStack stack) {
            return stack.getItem() instanceof ItemMinecart || stack.getItem() instanceof IMinecartItem;
        }

    },
    BALLAST {
        @Override
        protected boolean testType(ItemStack stack) {
            return BallastRegistry.isItemBallast(stack);
        }

    },
    EMPTY_BUCKET {
        @Override
        protected boolean testType(ItemStack stack) {
            if (stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null))
                return true;
           if (InvTools.isItem(stack, Items.BUCKET))
                return true;
            UniversalBucket uBucket = ForgeModContainer.getInstance().universalBucket;
            FluidStack fluidStack;
            return uBucket != null && of(UniversalBucket.class).test(stack) && (fluidStack = uBucket.getFluid(stack)) != null && fluidStack.amount <= 0;
        }

    },
    FLUID_CONTAINER {
        @Override
        protected boolean testType(ItemStack stack) {
            return stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        }
    },
    FEED {
        @Override
        protected boolean testType(ItemStack stack) {
            return stack.getItem() instanceof ItemFood || stack.getItem() == Items.WHEAT || stack.getItem() instanceof ItemSeeds;
        }

    },
    CARGO {
        @Override
        protected boolean testType(ItemStack stack) {
            return (RailcraftConfig.chestAllowLiquids() || !FluidItemHelper.isContainer(stack)) && RailcraftConfig.cargoBlacklist.stream().noneMatch(ing -> ing.apply(stack));
        }

    },
    RAW_METAL {
        @Override
        protected boolean testType(ItemStack stack) {
            return Stream.of(Metal.VALUES).anyMatch(m -> m.ingotFilter.test(stack) || m.blockFilter.test(stack) || m.nuggetFilter.test(stack));
        }
    };

    /**
     * Matches against the provided ItemStack.
     */
    public static Predicate<ItemStack> of(final ItemStack stack) {
        return stack1 -> InvTools.isItemEqual(stack1, stack);
    }

    /**
     * Matches against the provided class/interface.
     */
    public static Predicate<ItemStack> of(final Class<?> itemClass) {
        return stack -> !InvTools.isEmpty(stack) && itemClass.isAssignableFrom(stack.getItem().getClass());
    }

    /**
     * Matches against the provided Item.
     */
    public static Predicate<ItemStack> of(final Item item) {
        return stack -> !InvTools.isEmpty(stack) && stack.getItem() == item;
    }

    /**
     * Matches against the provided Item.
     */
    public static Predicate<ItemStack> of(final RailcraftItems item) {
        return stack -> !InvTools.isEmpty(stack) && item.isEqual(stack);
    }

    /**
     * Matches against the provided Item.
     */
    public static Predicate<ItemStack> of(final Block block) {
        return stack -> !InvTools.isEmpty(stack) && stack.getItem() == Item.getItemFromBlock(block);
    }

    /**
     * Matches against the provided ItemStacks. If the Item class extends IFilterItem then it will pass the check to the item.
     */
    public static Predicate<ItemStack> anyMatch(final ItemStack... filters) {
        return anyMatch(Arrays.asList(filters));
    }

    /**
     * Matches against the provided ItemStacks. If the Item class extends IFilterItem then it will pass the check to the item.
     */
    public static Predicate<ItemStack> anyMatch(final Collection<ItemStack> filters) {
        return stack -> filters.stream().anyMatch(f -> InvTools.matchesFilter(f, stack));
    }

    /**
     * Matches against the provided Inventory. If the Item class extends IFilterItem then it will pass the check to the item.
     */
    public static Predicate<ItemStack> anyMatch(final IInventoryComposite inv) {
        return stack -> inv.streamStacks().anyMatch(f -> InvTools.matchesFilter(f, stack));
    }

    /**
     * Matches against the provided ItemStacks.
     *
     * If no ItemStacks are provided to match against, it returns true.
     */
    public static Predicate<ItemStack> anyOf(final ItemStack... stacks) {
        return anyOf(Arrays.asList(stacks));
    }

    /**
     * Matches against the provided ItemStacks.
     *
     * If no ItemStacks are provided to match against, it returns true.
     */
    public static Predicate<ItemStack> anyOf(final Collection<ItemStack> stacks) {
        return stack -> stacks.isEmpty() || stacks.stream().allMatch(InvTools::isEmpty) || InvTools.isItemEqual(stack, stacks);
    }

    public static Predicate<ItemStack> none() {
        return itemStack -> false;
    }

    /**
     * Matches only if the given ItemStack does not match any of the provided ItemStacks.
     *
     * Returns false if the ItemStack being matched is null and true if the stacks to match against is empty/nulled.
     */
    public static Predicate<ItemStack> noneOf(final ItemStack... stacks) {
        return noneOf(Arrays.asList(stacks));
    }

    /**
     * Matches only if the given ItemStack does not match any of the provided ItemStacks.
     *
     * Returns false if the ItemStack being matched is null and true if the stacks to match against is empty/nulled.
     */
    public static Predicate<ItemStack> noneOf(final Collection<ItemStack> stacks) {
        return stack -> {
            if (InvTools.isEmpty(stack))
                return false;
            return stacks.stream()
                    .filter(InvTools::nonEmpty)
                    .noneMatch(filter -> InvTools.isItemEqual(stack, filter));
        };
    }

    /**
     * Matches if the given ItemStack is registered as a specific OreType in the Ore Dictionary.
     */
    public static Predicate<ItemStack> ofOreType(final String oreTag) {
        return stack -> OreDictPlugin.isOreType(oreTag, stack);
    }

    public static Predicate<ItemStack> ofSize(final int size) {
        return stack -> InvTools.sizeOf(stack) == size;
    }

    public static Predicate<ItemStack> singleton() {
        return stack -> InvTools.sizeOf(stack) == 1;
    }

    public static Predicate<ItemStack> nonEmpty() {
        return InvTools::nonEmpty;
    }

    /**
     * Matches if the Inventory contains the given ItemStack.
     */
    public static Predicate<ItemStack> containedIn(final IInventoryComposite inv) {
        return inv::contains;
    }

    /**
     * Matches if the Inventory has room and accepts the given ItemStack
     */
    public static Predicate<ItemStack> roomIn(final IInventoryComposite inv) {
        return inv::canFit;
    }

    /**
     * Matches if the ItemStack matches the given cart.
     */
    public static Predicate<ItemStack> isCart(@Nullable final EntityMinecart cart) {
        return stack -> {
            if (InvTools.isEmpty(stack))
                return false;
            if (cart == null)
                return false;
            if (cart instanceof IMinecart) {
                if (stack.hasDisplayName())
                    return ((IMinecart) cart).doesCartMatchFilter(stack, cart) && stack.getDisplayName().equals(cart.getCartItem().getDisplayName());
                return ((IMinecart) cart).doesCartMatchFilter(stack, cart);
            }
            ItemStack cartItem = cart.getCartItem();
            return !InvTools.isEmpty(stack) && InvTools.isCartItemEqual(stack, cartItem, true);
        };
    }

    protected boolean testType(ItemStack stack) {
        return true;
    }

    @Override
    public boolean test(ItemStack stack) {
        return !InvTools.isEmpty(stack) && testType(stack);
    }
}
