/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2019
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.blocks.single;

import mods.railcraft.common.blocks.TileRailcraft;
import mods.railcraft.common.fluids.FluidTools;
import mods.railcraft.common.fluids.Fluids;
import mods.railcraft.common.fluids.tanks.FilteredTank;
import mods.railcraft.common.gui.EnumGui;
import mods.railcraft.common.gui.GuiHandler;
import mods.railcraft.common.plugins.buildcraft.power.MjPlugin;
import mods.railcraft.common.plugins.forge.FuelPlugin;
import mods.railcraft.common.util.inventory.InvTools;
import mods.railcraft.common.util.inventory.InventoryAdvanced;
import mods.railcraft.common.util.inventory.ItemHandlerFactory;
import mods.railcraft.common.util.inventory.filters.StackFilters;
import mods.railcraft.common.util.inventory.wrappers.InventoryMapper;
import mods.railcraft.common.util.misc.Game;
import mods.railcraft.common.util.steam.IBoilerContainer;
import mods.railcraft.common.util.steam.SolidFuelProvider;
import mods.railcraft.common.util.steam.SteamBoiler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nullable;

import static mods.railcraft.common.util.inventory.InvTools.sizeOf;

/**
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class TileEngineSteamHobby extends TileEngineSteam implements ISidedInventory, IBoilerContainer {
    private static final long OUTPUT_MJ = 2 * MjPlugin.MJ;
    private static final long CAPACITY = 10000 * MjPlugin.MJ;
    private static final long RECEIVE = 300 * MjPlugin.MJ;
    private static final long EXTRACT = 40 * MjPlugin.MJ;

    public static final byte SLOT_FUEL = 0;
    public static final byte SLOT_LIQUID_INPUT = 1;
    public static final byte SLOT_LIQUID_OUTPUT = 2;
    private static final float FUEL_PER_CONVERSION_MULTIPLIER = 1.25F;
    private static final byte TICKS_PER_BOILER_CYCLE = 20;
    private static final int[] SLOTS = InvTools.buildSlotArray(0, 3);
    private static final int[] NO_SLOTS = new int[0];
    public final SteamBoiler boiler;
    private final InventoryAdvanced inv = new InventoryAdvanced(3).callbackInv(this);
    private final InventoryMapper invFuel = InventoryMapper.make(inv, SLOT_FUEL, 1);
    private final InventoryMapper invOutput = InventoryMapper.make(inv, SLOT_LIQUID_OUTPUT, 1);
    private boolean explode;

    public TileEngineSteamHobby() {
        FilteredTank tankWater = new FilteredTank(4 * FluidTools.BUCKET_VOLUME, this) {
            @Override
            public int fillInternal(@Nullable FluidStack resource, boolean doFill) {
                return super.fillInternal(onFillWater(resource), doFill);
            }
        }.setFilterFluid(Fluids.WATER);
        tankManager.add(tankWater);
        tankSteam.setCapacity(4 * FluidTools.BUCKET_VOLUME);

        boiler = new SteamBoiler(tankWater, tankSteam);
        boiler.setTicksPerCycle(TICKS_PER_BOILER_CYCLE);
        boiler.setEfficiencyModifier(FUEL_PER_CONVERSION_MULTIPLIER);
        boiler.setFuelProvider(new SolidFuelProvider(inv, SLOT_FUEL) {
            @Override
            public double getMoreFuel() {
                if (getEnergyStage() == EnergyStage.OVERHEAT || !isPowered()) return 0;
                return super.getMoreFuel();
            }
        });
    }

    @Override
    public SteamBoiler getBoiler() {
        return boiler;
    }

    @Override
    public EnumGui getGui() {
        return EnumGui.ENGINE_HOBBY;
    }

    @Override
    public boolean openGui(EntityPlayer player) {
        GuiHandler.openGui(EnumGui.ENGINE_HOBBY, player, world, getPos());
        return true;
    }

    @Override
    public boolean blockActivated(EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        return FluidUtil.interactWithFluidHandler(player, hand, getTankManager()) || super.blockActivated(player, hand, side, hitX, hitY, hitZ);
    }

    @Override
    public long getMaxOutputMJ() {
        return OUTPUT_MJ;
    }

    @Override
    public int steamUsedPerTick() {
        return 10;
    }

    @Override
    public void update() {
        super.update();
        if (Game.isHost(world)) {
            if (explode) {
                world.createExplosion(null, getX(), getY(), getZ(), 2, true);
                explode = false;
            }
        }
    }

    @Override
    public void burn() {
        super.burn();
        if(clock % FluidTools.BUCKET_FILL_TIME == 0)
            FluidTools.drainContainers(tankManager, inv, SLOT_LIQUID_INPUT, SLOT_LIQUID_OUTPUT);

        boiler.tick(1);

        if (StackFilters.EMPTY_BUCKET.test(getStackInSlot(SLOT_FUEL)))
            invFuel.moveOneItemTo(invOutput, StackFilters.EMPTY_BUCKET);
    }

    @Override
    public int getSizeInventory() {
        return inv.getSizeInventory();
    }

    @Override
    public boolean isEmpty() {
        return inv.isEmpty();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return inv.getStackInSlot(slot);
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        return inv.decrStackSize(slot, amount);
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        inv.setInventorySlotContents(slot, stack);
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
    public boolean isUsableByPlayer(EntityPlayer player) {
        return TileRailcraft.isUsableByPlayerHelper(this, player);
    }

    @Override
    public long maxEnergy() {
        return CAPACITY;
    }

    @Override
    public long maxEnergyReceived() {
        return RECEIVE;
    }

    @Override
    public long maxEnergyExtracted() {
        return EXTRACT;
    }

    @Override
    public boolean needsFuel() {
        ItemStack fuel = inv.getStackInSlot(SLOT_FUEL);
        return sizeOf(fuel) < 8;
    }

    @Override
    public float getTemperature() {
        return (float) boiler.getHeat();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        inv.writeToNBT("Items", data);
        boiler.writeToNBT(data);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        inv.readFromNBT("Items", data);

        boiler.readFromNBT(data);
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        if (getFacing() == side)
            return NO_SLOTS;
        return SLOTS;
    }

    @Override
    public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
        return isItemValidForSlot(index, itemStackIn);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        return index == SLOT_LIQUID_OUTPUT;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        return inv.removeStackFromSlot(index);
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
        for (int i = 0; i < getSizeInventory(); i++) {
            setInventorySlotContents(i, ItemStack.EMPTY);
        }
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        switch (slot) {
            case SLOT_FUEL:
                return FuelPlugin.getBurnTime(stack) > 0;
            case SLOT_LIQUID_INPUT:
                return Fluids.WATER.is(FluidUtil.getFluidContained(stack));
        }
        return false;
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public void steamExplosion(FluidStack resource) {
        explode = true;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(ItemHandlerFactory.wrap(this, facing));
        }
        return super.getCapability(capability, facing);
    }
}
