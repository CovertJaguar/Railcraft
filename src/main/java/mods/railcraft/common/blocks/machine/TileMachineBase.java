/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2017
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.blocks.machine;

import mods.railcraft.api.core.IPostConnection.ConnectStyle;
import mods.railcraft.api.core.items.IActivationBlockingItem;
import mods.railcraft.api.core.items.ITrackItem;
import mods.railcraft.common.blocks.RailcraftTickingTileEntity;
import mods.railcraft.common.blocks.machine.charge.BlockChargeFeeder;
import mods.railcraft.common.blocks.tracks.TrackTools;
import mods.railcraft.common.gui.EnumGui;
import mods.railcraft.common.gui.GuiHandler;
import mods.railcraft.common.util.inventory.InvTools;
import mods.railcraft.common.util.inventory.wrappers.InventoryMapper;
import mods.railcraft.common.util.misc.Game;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class TileMachineBase extends RailcraftTickingTileEntity {

    private boolean checkedBlock;

    public abstract IEnumMachine<?> getMachineType();

    @Override
    public String getLocalizationTag() {
        return getMachineType().getLocalizationTag() + ".name";
    }

    @Override
    public final short getId() {
        return (short) getMachineType().ordinal();
    }

    public boolean canCreatureSpawn(EntityLiving.SpawnPlacementType type) {
        return true;
    }

    public List<ItemStack> getDrops(int fortune) {
        ArrayList<ItemStack> items = new ArrayList<ItemStack>();
        items.add(getMachineType().getStack());
        return items;
    }

    public List<ItemStack> getBlockDroppedSilkTouch(int fortune) {
        return getDrops(fortune);
    }

    public boolean canSilkHarvest(EntityPlayer player) {
        return false;
    }

    public void initFromItem(ItemStack stack) {
    }

    public void onBlockAdded() {
    }

    /**
     * Called before the block is removed.
     */
    public void onBlockRemoval() {
        if (this instanceof IInventory)
            InvTools.dropInventory(new InventoryMapper((IInventory) this), worldObj, getPos());
    }

    public boolean blockActivated(EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (player.isSneaking())
            return false;
        if (!InvTools.isEmpty(heldItem)) {
            if (heldItem.getItem() instanceof IActivationBlockingItem)
                return false;
            if (heldItem.getItem() instanceof ITrackItem)
                return false;
            if (TrackTools.isRailItem(heldItem.getItem()))
                return false;
        }
        return openGui(player);
    }

    public boolean openGui(EntityPlayer player) {
        EnumGui gui = getGui();
        if (gui != null) {
            GuiHandler.openGui(gui, player, worldObj, getX(), getY(), getZ());
            return true;
        }
        return false;
    }

    @Nullable
    public EnumGui getGui() {
        return null;
    }

    public boolean isSideSolid(EnumFacing side) {
        return true;
    }

    @Override
    public void update() {
        super.update();

        if (Game.isClient(worldObj))
            return;

        // Check and fix invalid block ids
        if (!checkedBlock) {
            checkedBlock = true;

            if (!getMachineType().isAvailable()) {
                worldObj.setBlockToAir(getPos());
                return;
            }

            if (getBlockType() != getMachineType().block()) {
                Game.log(Level.INFO, "Updating Machine Block: {0} {1}->{2}, [{3}]", getClass().getSimpleName(), getBlockType(), getMachineType().block(), getPos());
                worldObj.setBlockState(getPos(), getMachineType().getDefaultState(), 3);
                validate();
                worldObj.setTileEntity(getPos(), this);
                updateContainingBlockInfo();
            }

            IBlockState oldState = worldObj.getBlockState(getPos());
            IEnumMachine variant = (IEnumMachine) ((BlockMachine) oldState.getBlock()).getVariant(oldState);
            if (getMachineType() != variant) {
                IBlockState newState = getMachineType().getDefaultState();
                if (newState != null) {
                    worldObj.setBlockState(getPos(), newState, 3);
                    validate();
                    worldObj.setTileEntity(getPos(), this);
                    Game.log(Level.INFO, "Updating Machine State: {0} {1}->{2}, [{3}]", getClass().getSimpleName(), oldState, newState, getPos());
                    updateContainingBlockInfo();
                }
            }
        }
    }

    public int getLightValue() {
        return 0;
    }

    public float getResistance(Entity exploder) {
        return 4.5f;
    }

    public float getHardness() {
        return 2.0f;
    }

    public boolean isPoweringTo(EnumFacing side) {
        return false;
    }

    public boolean canConnectRedstone(EnumFacing dir) {
        return false;
    }

    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(Random rand) {
    }

    public int colorMultiplier() {
        return 16777215;
    }

    public boolean recolourBlock(EnumDyeColor color) {
        return false;
    }

    public ConnectStyle connectsToPost(EnumFacing side) {
        if (isSideSolid(side.getOpposite()))
            return ConnectStyle.TWO_THIN;
        return ConnectStyle.NONE;
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
        return !(oldState.getBlock() == getBlockType() && newSate.getBlock() == getBlockType()
                && ((BlockChargeFeeder) getBlockType()).getVariant(oldState) == ((BlockChargeFeeder) getBlockType()).getVariant(newSate));
    }
}
