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

import mods.railcraft.common.blocks.RailcraftTileEntity;
import mods.railcraft.common.blocks.machine.beta.TileEngineSteam;
import mods.railcraft.common.gui.widgets.FluidGaugeWidget;
import mods.railcraft.common.gui.widgets.IndicatorWidget;
import mods.railcraft.common.gui.widgets.RFEnergyIndicator;
import mods.railcraft.common.util.network.PacketBuilder;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ContainerEngineSteam extends RailcraftContainer {

    private final TileEngineSteam tile;
    private int lastEnergy;
    private float lastOutput;
    private final RFEnergyIndicator energyIndicator;

    public ContainerEngineSteam(InventoryPlayer inventoryplayer, TileEngineSteam tile) {
        this.tile = tile;

        addWidget(new FluidGaugeWidget(tile.getTankManager().get(0), 71, 23, 176, 0, 16, 47));

        energyIndicator = new RFEnergyIndicator(tile.maxEnergy());
        addWidget(new IndicatorWidget(energyIndicator, 94, 25, 176, 47, 6, 43));

        for (int i = 0; i < 3; i++) {
            for (int k = 0; k < 9; k++) {
                addSlot(new Slot(inventoryplayer, k + i * 9 + 9, 8 + k * 18, 84 + i * 18));
            }
        }

        for (int j = 0; j < 9; j++) {
            addSlot(new Slot(inventoryplayer, j, 8 + j * 18, 142));
        }
    }

    @Override
    public void addListener(IContainerListener crafter) {
        super.addListener(crafter);

        PacketBuilder.instance().sendGuiIntegerPacket(crafter, windowId, 12, tile.energy);
        crafter.sendProgressBarUpdate(this, 14, Math.round(tile.currentOutput * 100));
    }

    @Override
    public void sendUpdateToClient() {
        super.sendUpdateToClient();

        for (IContainerListener crafter : listeners) {
            if (lastEnergy != tile.energy)
                PacketBuilder.instance().sendGuiIntegerPacket(crafter, windowId, 13, tile.energy);

            if (lastOutput != tile.currentOutput)
                crafter.sendProgressBarUpdate(this, 14, Math.round(tile.currentOutput * 100));
        }

        this.lastEnergy = tile.energy;
        this.lastOutput = tile.currentOutput;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int value) {

        switch (id) {
            case 12:
                energyIndicator.setEnergy(value);
                break;
            case 13:
                energyIndicator.updateEnergy(value);
                break;
            case 14:
                tile.currentOutput = value / 100f;
                break;
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer entityplayer) {
        return RailcraftTileEntity.isUsableByPlayerHelper(tile, entityplayer);
    }

}
