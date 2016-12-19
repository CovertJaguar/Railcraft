/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2016
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.blocks.machine;

import mods.railcraft.common.blocks.RailcraftTileEntity;
import mods.railcraft.common.plugins.forge.LocalizationPlugin;
import mods.railcraft.common.util.inventory.InvTools;
import mods.railcraft.common.util.inventory.StandaloneInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author CovertJaguar <http://www.railcraft.info>
 */
public abstract class TileMultiBlockInventory extends TileMultiBlock implements IInventory {

    protected final StandaloneInventory inv;

    protected TileMultiBlockInventory(int invSize, List<? extends MultiBlockPattern> patterns) {
        super(patterns);
        inv = new StandaloneInventory(invSize, (IInventory) this);
    }

    protected void dropItem(ItemStack stack) {
        InvTools.dropItem(stack, world, getPos());
    }

    @Override
    public ItemStack decrStackSize(int i, int j) {
        TileMultiBlockInventory mBlock = (TileMultiBlockInventory) getMasterBlock();
        if (mBlock != null)
            return mBlock.inv.decrStackSize(i, j);
        return null;
    }

    @Override
    public ItemStack getStackInSlot(int i) {
        TileMultiBlockInventory mBlock = (TileMultiBlockInventory) getMasterBlock();
        if (mBlock != null)
            return mBlock.inv.getStackInSlot(i);
        return null;
    }

    @Override
    public void setInventorySlotContents(int i, @Nullable ItemStack itemstack) {
        TileMultiBlockInventory mBlock = (TileMultiBlockInventory) getMasterBlock();
        if (mBlock != null)
            mBlock.inv.setInventorySlotContents(i, itemstack);
    }

    @Override
    public boolean isItemValidForSlot(int p_94041_1_, @Nullable ItemStack p_94041_2_) {
        return isStructureValid();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        inv.writeToNBT("invStructure", data);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        inv.readFromNBT("invStructure", data);
    }

    @Override
    public int getSizeInventory() {
        return inv.getSizeInventory();
    }

    @Override
    public String getName() {
        return LocalizationPlugin.translate(getLocalizationTag());
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void openInventory(EntityPlayer player) {
    }

    @Override
    public void closeInventory(EntityPlayer player) {
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return RailcraftTileEntity.isUsableByPlayerHelper(this, player);
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack inSlot = getStackInSlot(index);
        setInventorySlotContents(index, null);
        return inSlot;
    }

    @Override
    public int getField(int id) {
        return inv.getField(id);
    }

    @Override
    public void setField(int id, int value) {
        inv.setField(id, value);
    }

    @Override
    public int getFieldCount() {
        return inv.getFieldCount();
    }

    @Override
    public void clear() {
        inv.clear();
    }

    @Override
    public boolean isEmpty() {
        return inv.isEmpty();
    }
}
