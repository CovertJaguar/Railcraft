/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2019
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.blocks.multi;

import mods.railcraft.common.blocks.machine.ITankTile;
import mods.railcraft.common.fluids.TankManager;
import mods.railcraft.common.fluids.tanks.StandardTank;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.minecraft.util.EnumFacing.UP;

/**
 * @author CovertJaguar <http://www.railcraft.info>
 */
public abstract class TileTank extends TileMultiBlockInventory implements ITankTile, ISidedInventory {

    protected final TankManager tankManager = new TankManager();

    protected TileTank(int invNum, List<MultiBlockPattern> patterns) {
        super(invNum, patterns);
    }

    @Override
    public boolean blockActivated(EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        return (isStructureValid() && FluidUtil.interactWithFluidHandler(player, hand, getTankManager())) || super.blockActivated(player, hand, side, hitX, hitY, hitZ);
    }

    @Override
    public TankManager getTankManager() {
        TileMultiBlock mBlock = getMasterBlock();
        if (mBlock instanceof TileTank) {
            return ((TileTank) mBlock).tankManager;
        }
        return TankManager.NIL;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
            return (T) getTankManager();
        return super.getCapability(capability, facing);
    }

    @Override
    public @Nullable StandardTank getTank() {
        TileTank mBlock = (TileTank) getMasterBlock();
        if (mBlock != null) {
            return mBlock.tankManager.get(0);
        }
        return null;
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        if (side == UP) {
            return new int[]{0};
        }
        return new int[]{1};
    }

    @Override
    public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
        return true;
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        return true;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        tankManager.writeTanksToNBT(data);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        tankManager.readTanksFromNBT(data);
    }

    @Override
    public boolean isEmpty() {
        return inv.isEmpty();
    }
}
