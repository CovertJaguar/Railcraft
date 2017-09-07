/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2017
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.gui.containers;

import mods.railcraft.common.gui.slots.SlotRailcraft;
import mods.railcraft.common.gui.widgets.Widget;
import mods.railcraft.common.util.inventory.InvTools;
import mods.railcraft.common.util.network.PacketBuilder;
import mods.railcraft.common.util.network.RailcraftInputStream;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static mods.railcraft.common.util.inventory.InvTools.*;

/**
 * @author CovertJaguar <http://www.railcraft.info>
 */
public abstract class RailcraftContainer extends Container {
    @Nullable
    private final IInventory callback;
    private final List<Widget> widgets = new ArrayList<>();

    protected RailcraftContainer(IInventory inv) {
        this.callback = inv;
    }

    protected RailcraftContainer() {
        this.callback = null;
    }

    public List<Widget> getWidgets() {
        return widgets;
    }

    public void addSlot(Slot slot) {
        addSlotToContainer(slot);
    }

    public void addWidget(Widget widget) {
        widgets.add(widget);
        widget.addToContainer(this);
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
//        detectAndSendChanges();
    }

    @Override
    public final void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
            sendUpdateToClient();
            sendWidgetsServerData();
        }
    }

    private void sendWidgetsServerData() {
        widgets.forEach(this::sendWidgetServerData);
    }

    private void sendWidgetServerData(Widget widget) {
        listeners.forEach(l -> PacketBuilder.instance().sendGuiWidgetPacket(l, windowId, widget));
    }

    public void sendUpdateToClient() {
    }

    @SideOnly(Side.CLIENT)
    public void updateString(byte id, String data) {
    }

    @SideOnly(Side.CLIENT)
    public void updateData(byte id, RailcraftInputStream data) {
    }

    @Override
    public boolean canInteractWith(EntityPlayer entityplayer) {
        return callback == null || callback.isUseableByPlayer(entityplayer);
    }

    //TODO: test new parameters
    @Nullable
    @Override
    public ItemStack slotClick(int slotId, int mouseButton, ClickType clickType, EntityPlayer player) {
        Slot slot = slotId < 0 ? null : inventorySlots.get(slotId);
        if (slot instanceof SlotRailcraft && ((SlotRailcraft) slot).isPhantom())
            return slotClickPhantom((SlotRailcraft) slot, mouseButton, clickType, player);
        return super.slotClick(slotId, mouseButton, clickType, player);
    }

    @Nullable
    protected ItemStack slotClickPhantom(SlotRailcraft slot, int mouseButton, ClickType clickType, EntityPlayer player) {
        ItemStack stack = InvTools.emptyStack();

        if (mouseButton == 2) {
            if (slot.canAdjustPhantom())
                slot.putStack(null);
        } else if (mouseButton == 0 || mouseButton == 1) {
            InventoryPlayer playerInv = player.inventory;
            slot.onSlotChanged();
            ItemStack stackSlot = slot.getStack();
            ItemStack stackHeld = playerInv.getItemStack();

            if (!InvTools.isEmpty(stackSlot))
                stack = stackSlot.copy();

            if (InvTools.isEmpty(stackSlot)) {
                if (!InvTools.isEmpty(stackHeld) && slot.isItemValid(stackHeld))
                    fillPhantomSlot(slot, stackHeld, mouseButton);
            } else if (InvTools.isEmpty(stackHeld)) {
                adjustPhantomSlot(slot, mouseButton, clickType);
                slot.onPickupFromSlot(player, playerInv.getItemStack());
            } else if (slot.isItemValid(stackHeld))
                if (InvTools.isItemEqual(stackSlot, stackHeld))
                    adjustPhantomSlot(slot, mouseButton, clickType);
                else
                    fillPhantomSlot(slot, stackHeld, mouseButton);
        }
        return stack;
    }

    protected void adjustPhantomSlot(SlotRailcraft slot, int mouseButton, ClickType clickType) {
        if (!slot.canAdjustPhantom())
            return;
        ItemStack stackSlot = slot.getStack();
        if (InvTools.isEmpty(stackSlot))
            return;
        int stackSize;
        if (clickType == ClickType.QUICK_MOVE)
            stackSize = mouseButton == 0 ? (sizeOf(stackSlot) + 1) / 2 : sizeOf(stackSlot) * 2;
        else
            stackSize = mouseButton == 0 ? sizeOf(stackSlot) - 1 : sizeOf(stackSlot) + 1;

        if (stackSize > slot.getSlotStackLimit())
            stackSize = slot.getSlotStackLimit();

        setSize(stackSlot, stackSize);

        if (InvTools.isEmpty(stackSlot))
            slot.putStack(InvTools.emptyStack());
    }

    protected void fillPhantomSlot(SlotRailcraft slot, ItemStack stackHeld, int mouseButton) {
        if (!slot.canAdjustPhantom())
            return;
        int stackSize = mouseButton == 0 ? sizeOf(stackHeld) : 1;
        if (stackSize > slot.getSlotStackLimit())
            stackSize = slot.getSlotStackLimit();
        ItemStack phantomStack = stackHeld.copy();
        setSize(phantomStack, stackSize);

        slot.putStack(phantomStack);
    }

    protected boolean shiftItemStack(ItemStack stackToShift, int start, int end) {
        boolean changed = false;
        if (stackToShift.isStackable())
            for (int slotIndex = start; !isEmpty(stackToShift) && slotIndex < end; slotIndex++) {
                Slot slot = inventorySlots.get(slotIndex);
                ItemStack stackInSlot = slot.getStack();
                if (!InvTools.isEmpty(stackInSlot) && InvTools.isItemEqual(stackInSlot, stackToShift)) {
                    int resultingStackSize = sizeOf(stackInSlot) + sizeOf(stackToShift);
                    int max = Math.min(stackToShift.getMaxStackSize(), slot.getSlotStackLimit());
                    if (resultingStackSize <= max) {
                        setSize(stackToShift, 0);
                        setSize(stackInSlot, resultingStackSize);
                        slot.onSlotChanged();
                        changed = true;
                    } else if (sizeOf(stackInSlot) < max) {
                        decSize(stackToShift, max - sizeOf(stackInSlot));
                        setSize(stackInSlot, max);
                        slot.onSlotChanged();
                        changed = true;
                    }
                }
            }
        if (!isEmpty(stackToShift))
            for (int slotIndex = start; !isEmpty(stackToShift) && slotIndex < end; slotIndex++) {
                Slot slot = inventorySlots.get(slotIndex);
                ItemStack stackInSlot = slot.getStack();
                if (InvTools.isEmpty(stackInSlot)) {
                    int max = Math.min(stackToShift.getMaxStackSize(), slot.getSlotStackLimit());
                    stackInSlot = stackToShift.copy();
                    setSize(stackInSlot, Math.min(sizeOf(stackToShift), max));
                    decSize(stackToShift, sizeOf(stackInSlot));
                    slot.putStack(stackInSlot);
                    slot.onSlotChanged();
                    changed = true;
                }
            }
        return changed;
    }

    protected boolean tryShiftItem(ItemStack stackToShift, int numSlots) {
        for (int machineIndex = 0; machineIndex < numSlots - 9 * 4; machineIndex++) {
            Slot slot = inventorySlots.get(machineIndex);
            if (slot instanceof SlotRailcraft) {
                SlotRailcraft slotRailcraft = (SlotRailcraft) slot;
                if (slotRailcraft.isPhantom())
                    continue;
                if (!slotRailcraft.canShift())
                    continue;
            }
            if (!slot.isItemValid(stackToShift))
                continue;
            if (shiftItemStack(stackToShift, machineIndex, machineIndex + 1))
                return true;
        }
        return false;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) {
        ItemStack originalStack = null;
        Slot slot = inventorySlots.get(slotIndex);
        int numSlots = inventorySlots.size();
        if (slot != null && slot.getHasStack()) {
            ItemStack stackInSlot = slot.getStack();
            assert !InvTools.isEmpty(stackInSlot);
            originalStack = stackInSlot.copy();
            if (!(slotIndex >= numSlots - 9 * 4 && tryShiftItem(stackInSlot, numSlots))) {
                if (slotIndex >= numSlots - 9 * 4 && slotIndex < numSlots - 9) {
                    if (!shiftItemStack(stackInSlot, numSlots - 9, numSlots))
                        return InvTools.emptyStack();
                } else if (slotIndex >= numSlots - 9 && slotIndex < numSlots) {
                    if (!shiftItemStack(stackInSlot, numSlots - 9 * 4, numSlots - 9))
                        return InvTools.emptyStack();
                } else if (!shiftItemStack(stackInSlot, numSlots - 9 * 4, numSlots))
                    return InvTools.emptyStack();
            }
            slot.onSlotChange(stackInSlot, originalStack);
            if (isEmpty(stackInSlot))
                slot.putStack(InvTools.emptyStack());
            else
                slot.onSlotChanged();
            if (sizeOf(stackInSlot) == sizeOf(originalStack))
                return InvTools.emptyStack();
            slot.onPickupFromSlot(player, stackInSlot);
        }
        return originalStack;
    }

}
