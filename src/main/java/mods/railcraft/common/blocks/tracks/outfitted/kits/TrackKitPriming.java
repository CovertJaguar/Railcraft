/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2016
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.blocks.tracks.outfitted.kits;

import mods.railcraft.api.carts.IExplosiveCart;
import mods.railcraft.api.core.items.IToolCrowbar;
import mods.railcraft.common.blocks.tracks.outfitted.TrackKits;
import mods.railcraft.common.gui.EnumGui;
import mods.railcraft.common.gui.GuiHandler;
import mods.railcraft.common.util.inventory.InvTools;
import mods.railcraft.common.util.network.IGuiReturnHandler;
import mods.railcraft.common.util.network.RailcraftInputStream;
import mods.railcraft.common.util.network.RailcraftOutputStream;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TrackKitPriming extends TrackKitPowered implements IGuiReturnHandler {

    public static final short MAX_FUSE = 500;
    public static final short MIN_FUSE = 0;
    private short fuse = 80;

    @Nullable
    @Override
    public World theWorld() {
        return super.theWorld();
    }

    @Override
    public TrackKits getTrackKitContainer() {
        return TrackKits.PRIMING;
    }

    @Override
    public boolean blockActivated(EntityPlayer player, EnumHand hand, ItemStack heldItem) {
        if (!InvTools.isEmpty(heldItem) && heldItem.getItem() instanceof IToolCrowbar) {
            IToolCrowbar crowbar = (IToolCrowbar) heldItem.getItem();
            if (crowbar.canWhack(player, hand, heldItem, getPos())) {
                GuiHandler.openGui(EnumGui.TRACK_PRIMING, player, theWorldAsserted(), getPos());
                crowbar.onWhack(player, hand, heldItem, getPos());
                return true;
            }
        }
        return false;
    }

    @Override
    public void onMinecartPass(EntityMinecart cart) {
        if (isPowered()) {
            if (cart instanceof IExplosiveCart) {
                IExplosiveCart tnt = (IExplosiveCart) cart;
                tnt.setFuse(fuse);
                tnt.setPrimed(true);
            }
        }
    }

    @Override
    public void writePacketData(DataOutputStream data) throws IOException {
        super.writePacketData(data);
        data.writeShort(fuse);
    }

    @Override
    public void readPacketData(DataInputStream data) throws IOException {
        super.readPacketData(data);
        fuse = data.readShort();

        markBlockNeedsUpdate();
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setShort("fuse", getFuse());
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        setFuse(data.getShort("fuse"));
    }

    @Override
    public void writeGuiData(RailcraftOutputStream data) throws IOException {
        data.writeShort(fuse);
    }

    @Override
    public void readGuiData(RailcraftInputStream data, EntityPlayer sender) throws IOException {
        fuse = data.readShort();
    }

    public short getFuse() {
        return fuse;
    }

    public void setFuse(short f) {
        f = (short) Math.max(f, MIN_FUSE);
        f = (short) Math.min(f, MAX_FUSE);
        fuse = f;
    }
}
