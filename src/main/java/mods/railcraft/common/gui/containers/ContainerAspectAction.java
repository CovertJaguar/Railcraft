/*
 * Copyright (c) CovertJaguar, 2011-2017
 * http://railcraft.info
 *
 * This code is the property of CovertJaguar
 * and may only be used with explicit written
 * permission unless otherwise specified on the
 * license page at http://railcraft.info/wiki/info:license.
 */
package mods.railcraft.common.gui.containers;

import mods.railcraft.common.blocks.machine.interfaces.ITileAspectResponder;
import mods.railcraft.common.plugins.forge.PlayerPlugin;
import mods.railcraft.common.util.network.PacketBuilder;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IContainerListener;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ContainerAspectAction extends RailcraftContainer {

    private final ITileAspectResponder actionManager;
    private final EntityPlayer player;
    private int lastLockState;
    public boolean canLock;
    public String ownerName;

    public ContainerAspectAction(EntityPlayer player, ITileAspectResponder actionManager) {
        this.actionManager = actionManager;
        this.player = player;
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);

        listener.sendProgressBarUpdate(this, 0, actionManager.getLockController().getCurrentState());
        listener.sendProgressBarUpdate(this, 1, PlayerPlugin.isOwnerOrOp(actionManager.getOwner(), player) ? 1 : 0);

        String username = actionManager.getOwner().getName();
        if (username != null)
            PacketBuilder.instance().sendGuiStringPacket(listener, windowId, 0, username);
    }

    @Override
    public void sendUpdateToClient() {
        super.sendUpdateToClient();

        for (IContainerListener crafter : listeners) {
            int lock = actionManager.getLockController().getCurrentState();
            if (lastLockState != lock)
                crafter.sendProgressBarUpdate(this, 0, lock);
        }

        this.lastLockState = actionManager.getLockController().getCurrentState();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int value) {
        switch (id) {
            case 0:
                actionManager.getLockController().setCurrentState(value);
                break;
            case 1:
                canLock = value == 1;
                break;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateString(byte id, String data) {
        switch (id) {
            case 0:
                try {
                    ownerName = data;
                } catch (IllegalArgumentException ignored) {
                }
                break;
        }
    }

}
