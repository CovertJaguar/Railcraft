/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2017
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/

package mods.railcraft.common.util.inventory;

import mods.railcraft.common.plugins.forge.WorldPlugin;
import mods.railcraft.common.util.inventory.wrappers.ChestWrapper;
import mods.railcraft.common.util.inventory.wrappers.IInventoryObject;
import mods.railcraft.common.util.inventory.wrappers.InventoryAdaptor;
import mods.railcraft.common.util.inventory.wrappers.SidedInventoryDecorator;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nullable;
import java.util.function.Predicate;

/**
 * Created by CovertJaguar on 5/28/2017 for Railcraft.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class InventoryFactory {
    private InventoryFactory() {
    }

    @Nullable
    public static IInventoryObject get(World world, BlockPos pos, EnumFacing side, @Nullable final Class<? extends TileEntity> type, @Nullable final Class<? extends TileEntity> exclude) {
        return get(world, pos, side, tile -> {
            //noinspection SimplifiableIfStatement
            if (type != null && !type.isAssignableFrom(tile.getClass()))
                return false;
            return exclude == null || !exclude.isAssignableFrom(tile.getClass());
        });
    }

    @Nullable
    public static IInventoryObject get(World world, BlockPos pos, EnumFacing side, Predicate<TileEntity> filter) {
        TileEntity tile = WorldPlugin.getBlockTile(world, pos.offset(side));
        if (!(tile instanceof IInventory) || !filter.test(tile))
            return null;
        return get(tile, side.getOpposite());
    }

    @Nullable
    public static IInventoryObject get(@Nullable Object obj, EnumFacing side) {
        if (obj == null)
            return null;

        if (obj instanceof TileEntityChest) {
            TileEntityChest chest = (TileEntityChest) obj;
            return new ChestWrapper(chest);
        } else if (obj instanceof ISidedInventory) {
            return new SidedInventoryDecorator((ISidedInventory) obj, side);
        } else if (obj instanceof IInventory) {
            return InventoryAdaptor.get((IInventory) obj);
        } else if (obj instanceof ICapabilityProvider && ((ICapabilityProvider) obj).hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side)) {
            return InventoryAdaptor.get(((ICapabilityProvider) obj).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side));
        }
        return null;
    }

    @Nullable
    public static IInventoryObject get(@Nullable Object obj) {
        if (obj == null)
            return null;

        if (obj instanceof IInventoryObject)
            return (IInventoryObject) obj;

        if (obj instanceof TileEntityChest) {
            TileEntityChest chest = (TileEntityChest) obj;
            return new ChestWrapper(chest);
        }

        if (obj instanceof IInventory)
            return InventoryAdaptor.get((IInventory) obj);

        if (obj instanceof ICapabilityProvider && ((ICapabilityProvider) obj).hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null))
            return InventoryAdaptor.get(((ICapabilityProvider) obj).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null));

        return null;
    }
}
