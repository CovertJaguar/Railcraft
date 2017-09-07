/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2017
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.util.inventory;

import com.google.common.collect.Iterators;
import mods.railcraft.api.tracks.ITrackKitInstance;
import mods.railcraft.common.blocks.RailcraftTileEntity;
import mods.railcraft.common.plugins.forge.LocalizationPlugin;
import mods.railcraft.common.util.inventory.filters.StackFilters;
import mods.railcraft.common.util.inventory.iterators.InventoryIterator;
import mods.railcraft.common.util.inventory.wrappers.IInventoryComposite;
import mods.railcraft.common.util.inventory.wrappers.IInventoryObject;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.function.Predicate;

import static mods.railcraft.common.util.inventory.InvTools.setSize;
import static mods.railcraft.common.util.inventory.InvTools.sizeOf;

/**
 * Creates a standalone instance of IInventory.
 * <p/>
 * Useful for hiding parts of an inventory from outsiders.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class StandaloneInventory implements IInventory, IInventoryObject, IInventoryComposite {

    @Nullable
    private final String name;
    @Nullable
    private final Callback callback;
    private final ItemStack[] contents;
    private int inventoryStackLimit = 64;
    @Nullable
    private Boolean isEmpty = Boolean.TRUE;

    public StandaloneInventory(int size, @Nullable String name, @Nullable IInventory callback) {
        this.name = name;
        contents = new ItemStack[size];
        this.callback = callback == null ? null : new InventoryCallback(callback);
    }

    public StandaloneInventory(int size, @Nullable String name, @Nullable RailcraftTileEntity callback) {
        this.name = name;
        contents = new ItemStack[size];
        this.callback = callback == null ? null : new TileCallback(callback);
    }

    public StandaloneInventory(int size, @Nullable String name, @Nullable ITrackKitInstance callback) {
        this.name = name;
        contents = new ItemStack[size];
        this.callback = callback == null ? null : new TrackKitCallback(callback);
    }

    public StandaloneInventory(int size, @Nullable String name, @Nullable Callback callback) {
        this.name = name;
        contents = new ItemStack[size];
        this.callback = callback;
    }

    public StandaloneInventory(int size, @Nullable IInventory callback) {
        this(size, null, callback);
    }

    public StandaloneInventory(int size, @Nullable RailcraftTileEntity callback) {
        this(size, null, callback);
    }

    public StandaloneInventory(int size, @Nullable ITrackKitInstance callback) {
        this(size, null, callback);
    }

    public StandaloneInventory(int size, @Nullable String name) {
        this(size, name, (RailcraftTileEntity) null);
    }

    public StandaloneInventory(int size) {
        this(size, null, (RailcraftTileEntity) null);
    }

    public Predicate<ItemStack> presenceTest() {
        return StackFilters.containedIn(this);
    }

    @Override
    public Object getBackingObject() {
        return this;
    }

    @Override
    public int getNumSlots() {
        return getSizeInventory();
    }

    @Override
    public int getSizeInventory() {
        return contents.length;
    }

    @Override
    @Nullable
    public ItemStack getStackInSlot(int i) {
        return contents[i];
    }

    @Override
    public ItemStack decrStackSize(int i, int j) {
        if (!InvTools.isEmpty(contents[i])) {
            if (sizeOf(contents[i]) <= j) {
                ItemStack itemstack = contents[i];
                contents[i] = InvTools.emptyStack();
                markDirty();
                return itemstack;
            }
            ItemStack itemStack1 = contents[i].splitStack(j);
            if (sizeOf(contents[i]) <= 0) {
                contents[i] = InvTools.emptyStack();
            }
            markDirty();
            return itemStack1;
        } else {
            return InvTools.emptyStack();
        }
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack stack = contents[index];
        contents[index] = InvTools.emptyStack();
        markDirty();
        return stack;
    }

    @Override
    public void setInventorySlotContents(int i, @Nullable ItemStack stack) {
        contents[i] = stack;
        if (sizeOf(stack) > getInventoryStackLimit()) {
            setSize(stack, getInventoryStackLimit());
        }
        markDirty();
    }

    @Override
    public boolean hasCustomName() {
        return name != null || (callback != null && callback.hasCustomName());
    }

    @Override
    public String getName() {
        if (name != null) {
            return LocalizationPlugin.translate(name);
        }
        if (callback != null && callback.hasCustomName()) {
            return callback.getName();
        }
        return invTypeName();
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentString(getName());
    }

    protected String invTypeName() {
        return "Standalone";
    }

    public void setInventoryStackLimit(int inventoryStackLimit) {
        this.inventoryStackLimit = inventoryStackLimit;
    }

    @Override
    public int getInventoryStackLimit() {
        return inventoryStackLimit;
    }

    @Override
    public void markDirty() {
        isEmpty = null;
        if (callback != null) {
            callback.markDirty();
        }
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer entityplayer) {
        return callback == null || callback.isUsableByPlayer(entityplayer);
    }

    @Override
    public void openInventory(EntityPlayer player) {
        if (callback != null) {
            callback.openInventory(player);
        }
    }

    @Override
    public void closeInventory(EntityPlayer player) {
        if (callback != null) {
            callback.closeInventory(player);
        }
    }

    public ItemStack[] getContents() {
        return contents;
    }

    public void writeToNBT(String tag, NBTTagCompound data) {
        InvTools.writeInvToNBT(this, tag, data);
    }

    public void readFromNBT(String tag, NBTTagCompound data) {
        NBTTagList list = data.getTagList(tag, 10);
        for (byte entry = 0; entry < list.tagCount(); entry++) {
            NBTTagCompound itemTag = list.getCompoundTagAt(entry);
            int slot = itemTag.getByte(InvTools.TAG_SLOT);
            if (slot >= 0 && slot < getSizeInventory()) {
                ItemStack stack = InvTools.readItemFromNBT(itemTag);
                contents[slot] = stack;
            }
        }
    }

    @Override
    public boolean isItemValidForSlot(int i, @Nullable ItemStack itemstack) {
        return true;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {

    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
        for (int i = 0; i < contents.length; i++) {
            contents[i] = null;
        }
        markDirty();
    }

    public Iterable<ItemStack> getStacks() {
        return InventoryIterator.getRailcraft(this).getStacks();
    }

    @Override
    public Iterator<IInventoryObject> iterator() {
        return Iterators.singletonIterator(this);
    }

    public boolean isEmpty() {
        if (isEmpty != null)
            return isEmpty;
        for (ItemStack stack : contents) {
            if (!InvTools.isEmpty(stack))
                return isEmpty = false;
        }
        return isEmpty = true;
    }

    public abstract static class Callback {

        public boolean isUsableByPlayer(EntityPlayer player) {
            return true;
        }

        public void openInventory(EntityPlayer player) {
        }

        public void closeInventory(EntityPlayer player) {
        }

        public void markDirty() {
        }

        public String getName() {
            return "Standalone";
        }

        public Boolean hasCustomName() {
            return false;
        }

    }

    private static class InventoryCallback extends Callback {

        private final IInventory inv;

        public InventoryCallback(IInventory inv) {
            this.inv = inv;
        }

        @Override
        public boolean isUsableByPlayer(EntityPlayer player) {
            return inv.isUseableByPlayer(player);
        }

        @Override
        public void openInventory(EntityPlayer player) {
            inv.openInventory(player);
        }

        @Override
        public void closeInventory(EntityPlayer player) {
            inv.closeInventory(player);
        }

        @Override
        public void markDirty() {
            inv.markDirty();
        }

        @Override
        public String getName() {
            return inv.getName();
        }

        @Override
        public Boolean hasCustomName() {
            return inv.hasCustomName();
        }

    }

    private static class TileCallback extends Callback {

        private final RailcraftTileEntity tile;

        public TileCallback(RailcraftTileEntity tile) {
            this.tile = tile;
        }

        @Override
        public void markDirty() {
            tile.markDirty();
        }

        @Override
        public String getName() {
            return tile.getName();
        }

        @Override
        public Boolean hasCustomName() {
            return true;
        }
    }

    private static class TrackKitCallback extends Callback {

        private final ITrackKitInstance instance;

        TrackKitCallback(ITrackKitInstance instance) {
            this.instance = instance;
        }

        @Override
        public void markDirty() {
            instance.markDirty();
        }

        @Override
        public String getName() {
            return ((RailcraftTileEntity) instance.getTile()).getName();
        }

        @Override
        public Boolean hasCustomName() {
            return true;
        }
    }
}
