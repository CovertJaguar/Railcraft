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
import mods.railcraft.common.blocks.tracks.outfitted.TrackKits;
import mods.railcraft.common.core.RailcraftConfig;
import mods.railcraft.common.gui.EnumGui;
import mods.railcraft.common.gui.GuiHandler;
import mods.railcraft.common.util.inventory.InvTools;
import mods.railcraft.common.util.misc.SafeNBTWrapper;
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

public class TrackKitLauncher extends TrackKitPowered implements IGuiReturnHandler {

    public static final int MIN_LAUNCH_FORCE = 5;
    private static final float LAUNCH_THRESHOLD = 0.01f;
    private byte launchForce = 5;

    @Nullable
    @Override
    public World theWorld() {
        return super.theWorld();
    }

    @Override
    public TrackKits getTrackKitContainer() {
        return TrackKits.LAUNCHER;
    }

    @Override
    public boolean blockActivated(EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem) {
        if (!InvTools.isEmpty(heldItem) && heldItem.getItem() instanceof IToolCrowbar) {
            IToolCrowbar crowbar = (IToolCrowbar) heldItem.getItem();
            if (crowbar.canWhack(player, hand, heldItem, getPos())) {
                GuiHandler.openGui(EnumGui.TRACK_LAUNCHER, player, theWorldAsserted(), getPos());
                crowbar.onWhack(player, hand, heldItem, getPos());
                return true;
            }
        }
        return false;
    }

    @Override
    public void onMinecartPass(EntityMinecart cart) {
        if (isPowered()) {
            if (Math.abs(cart.motionX) > LAUNCH_THRESHOLD) {
                cart.motionX = Math.copySign(0.6f, cart.motionX);
            }
            if (Math.abs(cart.motionZ) > LAUNCH_THRESHOLD) {
                cart.motionZ = Math.copySign(0.6f, cart.motionZ);
            }
            cart.setMaxSpeedAirLateral(0.6f);
            cart.setMaxSpeedAirVertical(0.5f);
            cart.setDragAir(0.99999);
            cart.motionY = getLaunchForce() * 0.1;
            cart.getEntityData().setInteger("Launched", 1);
            cart.setCanUseRail(false);
            cart.moveEntity(cart.motionX, 1.5, cart.motionZ);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setByte("force", getLaunchForce());
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);

        SafeNBTWrapper safe = new SafeNBTWrapper(data);

        setLaunchForce(safe.getByte("force"));
    }

    @Override
    public void writePacketData(DataOutputStream data) throws IOException {
        super.writePacketData(data);
        data.writeByte(launchForce);
    }

    @Override
    public void readPacketData(DataInputStream data) throws IOException {
        super.readPacketData(data);
        launchForce = data.readByte();
    }

    @Override
    public void writeGuiData(RailcraftOutputStream data) throws IOException {
        data.writeByte(launchForce);
    }

    @Override
    public void readGuiData(RailcraftInputStream data, EntityPlayer sender) throws IOException {
        launchForce = data.readByte();
    }

    public byte getLaunchForce() {
        return launchForce;
    }

    public void setLaunchForce(int force) {
        force = Math.max(force, MIN_LAUNCH_FORCE);
        force = Math.min(force, RailcraftConfig.getLaunchRailMaxForce());
        launchForce = (byte) force;
    }
}
