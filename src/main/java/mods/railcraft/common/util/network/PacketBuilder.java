/*
 * Copyright (c) CovertJaguar, 2011-2017
 * http://railcraft.info
 *
 * This code is the property of CovertJaguar
 * and may only be used with explicit written
 * permission unless otherwise specified on the
 * license page at http://railcraft.info/wiki/info:license.
 */
package mods.railcraft.common.util.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import mods.railcraft.api.signals.AbstractPair;
import mods.railcraft.api.signals.ISignalPacketBuilder;
import mods.railcraft.common.gui.widgets.Widget;
import mods.railcraft.common.util.misc.Game;
import mods.railcraft.common.util.network.PacketKeyPress.EnumKeyBinding;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.world.WorldServer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

/**
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class PacketBuilder implements ISignalPacketBuilder {

    private static PacketBuilder instance;

    private PacketBuilder() {
    }

    @Nonnull
    public static PacketBuilder instance() {
        if (instance == null)
            instance = new PacketBuilder();
        return instance;
    }

    public void sendTileEntityPacket(TileEntity tile) {
        if (tile.getWorld() instanceof WorldServer) {
            WorldServer world = (WorldServer) tile.getWorld();
            SPacketUpdateTileEntity packet = tile.getUpdatePacket();
            if (packet != null)
                PacketDispatcher.sendToWatchers(packet, world, tile.getPos().getX(), tile.getPos().getZ());
        }
    }

    public void sendTileEntityPacket(@Nullable TileEntity tile, EntityPlayerMP player) {
        if (tile != null) {
            SPacketUpdateTileEntity packet = tile.getUpdatePacket();
            if (packet != null)
                PacketDispatcher.sendToPlayer(packet, player);
        }
    }

    @Override
    public void sendPairPacketUpdate(AbstractPair pairing) {
        PacketPairUpdate pkt = new PacketPairUpdate(pairing);
        PacketDispatcher.sendToDimension(pkt, pairing.getTile().getWorld().provider.getDimension());
    }

    @Override
    public void sendPairPacketRequest(AbstractPair pairing) {
        PacketPairRequest pkt = new PacketPairRequest(pairing);
        PacketDispatcher.sendToServer(pkt);
    }

    public void sendGuiReturnPacket(IGuiReturnHandler handler) {
        PacketGuiReturn pkt = new PacketGuiReturn(handler);
        PacketDispatcher.sendToServer(pkt);
    }

    public void sendGuiReturnPacket(IGuiReturnHandler handler, byte[] extraData) {
        PacketGuiReturn pkt = new PacketGuiReturn(handler, extraData);
        PacketDispatcher.sendToServer(pkt);
    }

    public void sendKeyPressPacket(EnumKeyBinding keyPress) {
        PacketKeyPress pkt = new PacketKeyPress(keyPress);
        PacketDispatcher.sendToServer(pkt);
    }

    public void sendGuiIntegerPacket(IContainerListener listener, int windowId, int key, int value) {
        if (listener instanceof EntityPlayerMP) {
            PacketGuiInteger pkt = new PacketGuiInteger(windowId, key, value);
            PacketDispatcher.sendToPlayer(pkt, (EntityPlayerMP) listener);
        }
    }

    public void sendGuiStringPacket(IContainerListener listener, int windowId, int key, String value) {
        if (listener instanceof EntityPlayerMP) {
            PacketGuiString pkt = new PacketGuiString(windowId, key, value);
            PacketDispatcher.sendToPlayer(pkt, (EntityPlayerMP) listener);
        }
    }

    public void sendGuiDataPacket(IContainerListener listener, int windowId, int key, byte[] value) {
        if (listener instanceof EntityPlayerMP) {
            PacketGuiData pkt = new PacketGuiData(windowId, key, value);
            PacketDispatcher.sendToPlayer(pkt, (EntityPlayerMP) listener);
        }
    }

    public void sendGuiWidgetPacket(IContainerListener listener, int windowId, Widget widget) {
        if (listener instanceof EntityPlayerMP && widget.hasServerSyncData(listener)) {
            ByteBuf byteBuf = Unpooled.buffer();
            try (ByteBufOutputStream out = new ByteBufOutputStream(byteBuf);
                 RailcraftOutputStream data = new RailcraftOutputStream(out)) {
                widget.writeServerSyncData(listener, data);
                PacketGuiWidget pkt = new PacketGuiWidget(windowId, widget, byteBuf.array());
                PacketDispatcher.sendToPlayer(pkt, (EntityPlayerMP) listener);
            } catch (IOException ex) {
                if (Game.DEVELOPMENT_ENVIRONMENT)
                    throw new RuntimeException(ex);
            }
        }
    }

    public void sendGoldenTicketGuiPacket(EntityPlayerMP player, EnumHand hand) {
        PacketTicketGui pkt = new PacketTicketGui(hand);
        PacketDispatcher.sendToPlayer(pkt, player);
    }

}
