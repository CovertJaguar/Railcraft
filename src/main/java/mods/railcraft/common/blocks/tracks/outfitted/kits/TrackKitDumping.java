/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2016
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.blocks.tracks.outfitted.kits;

import mods.railcraft.api.core.items.IToolCrowbar;
import mods.railcraft.api.tracks.ITrackKitPowered;
import mods.railcraft.common.blocks.tracks.outfitted.TrackKits;
import mods.railcraft.common.carts.CartTools;
import mods.railcraft.common.gui.EnumGui;
import mods.railcraft.common.gui.GuiHandler;
import mods.railcraft.common.util.inventory.InvTools;
import mods.railcraft.common.util.inventory.InventoryFactory;
import mods.railcraft.common.util.inventory.PhantomInventory;
import mods.railcraft.common.util.inventory.filters.StackFilters;
import mods.railcraft.common.util.inventory.manipulators.InventoryManipulator;
import mods.railcraft.common.util.inventory.wrappers.IInventoryComposite;
import mods.railcraft.common.util.inventory.wrappers.IInventoryObject;
import mods.railcraft.common.util.inventory.wrappers.InventoryComposite;
import mods.railcraft.common.util.misc.Predicates;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.function.Predicate;

import static mods.railcraft.common.util.inventory.InvTools.isEmpty;

public class TrackKitDumping extends TrackKitSuspended implements ITrackKitPowered {

    private static final int TIME_TILL_NEXT_MOUNT = 40;
    private boolean powered;
    private PhantomInventory cartFilter = new PhantomInventory(3, this);
    private PhantomInventory itemFilter = new PhantomInventory(9, this);
    private Predicate<EntityMinecart> matchesCarts = ((Predicate<EntityMinecart>) (cart -> cartFilter.isEmpty())).or(CartTools.matchesCartsIfFiltered(cartFilter));
    private Predicate<ItemStack> matchesFilters = ((Predicate<ItemStack>) (stack -> itemFilter.isEmpty())).or(StackFilters.matchesAny(itemFilter));

    public IInventory getCartFilter() {
        return cartFilter;
    }

    public IInventory getItemFilter() {
        return itemFilter;
    }

    @Override
    public int getRenderState() {
        return powered ? 1 : 0;
    }

    @Override
    public TrackKits getTrackKitContainer() {
        return TrackKits.DUMPING;
    }

    @Override
    public void onMinecartPass(EntityMinecart cart) {
        if (isPowered())
            return;

        if (!matchesCarts.test(cart)) {
            return;
        }

        if (cart.isBeingRidden() && tryDumpRider(cart)) {
            return;
        }

        if (tryDumpInventory(cart)) {
            return;
        }
    }

    private boolean tryDumpRider(EntityMinecart cart) {
        World world = theWorldAsserted();
        BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain().setPos(getTile().getPos());
        for (int i = 0; i < 2; i++) {
            pos.move(EnumFacing.DOWN);
            if (world.getBlockState(pos).getBlock().isVisuallyOpaque())
                return false;
        }
        pos.release();
        if (cart.isBeingRidden()) {
            CartTools.removePassengers(cart, cart.getPositionVector().addVector(0, -2, 0));
        }
        cart.getEntityData().setInteger("MountPrevention", TIME_TILL_NEXT_MOUNT);
        return true;
    }

    private boolean tryDumpInventory(EntityMinecart cart) {
        IInventoryObject object = InventoryFactory.get(cart);
        if (object == null) {
            return false;
        }
        InventoryManipulator<?> manipulator = InventoryManipulator.get(object);
        ItemStack stack = manipulator.removeItem(matchesFilters);
        if (isEmpty(stack)) {
            return false;
        }

        IInventoryComposite composite = InventoryComposite.of(InventoryFactory.get(theWorldAsserted(), getPos(), EnumFacing.DOWN, Predicates.alwaysTrue()));
        stack = InvTools.moveItemStack(stack, composite);
        if (isEmpty(stack)) {
            return true;
        }

        //Dump now!
        InvTools.spewItem(stack, theWorldAsserted(), getPos().down());
        return true;
    }

    @Override
    public void onNeighborBlockChange(IBlockState state, @Nullable Block neighborBlock) {
        super.onNeighborBlockChange(state, neighborBlock);
        testPower(state);
    }

    @Override
    public boolean blockActivated(EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem) {
        if (!isEmpty(heldItem) && heldItem.getItem() instanceof IToolCrowbar) {
            IToolCrowbar crowbar = (IToolCrowbar) heldItem.getItem();
            if (crowbar.canWhack(player, hand, heldItem, getPos())) {
                GuiHandler.openGui(EnumGui.TRACK_DUMPING, player, theWorldAsserted(), getPos());
                crowbar.onWhack(player, hand, heldItem, getPos());
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isPowered() {
        return powered;
    }

    @Override
    public void setPowered(boolean powered) {
        this.powered = powered;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbttagcompound) {
        super.writeToNBT(nbttagcompound);
        nbttagcompound.setBoolean("powered", powered);
        cartFilter.writeToNBT("cartFilter", nbttagcompound);
        itemFilter.writeToNBT("itemFilter", nbttagcompound);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbttagcompound) {
        super.readFromNBT(nbttagcompound);
        powered = nbttagcompound.getBoolean("powered");
        cartFilter.readFromNBT("cartFilter", nbttagcompound);
        itemFilter.readFromNBT("itemFilter", nbttagcompound);
    }

    @Override
    public void writePacketData(DataOutputStream data) throws IOException {
        super.writePacketData(data);
        data.writeBoolean(powered);
    }

    @Override
    public void readPacketData(DataInputStream data) throws IOException {
        super.readPacketData(data);
        boolean p = data.readBoolean();
        if (p != powered) {
            powered = p;
            markBlockNeedsUpdate();
        }
    }
}
