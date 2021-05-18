/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2019
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.gui.containers;

import mods.railcraft.common.blocks.TileRailcraft;
import mods.railcraft.common.blocks.single.TileEngineSteam;
import mods.railcraft.common.gui.widgets.FluidGaugeWidget;
import mods.railcraft.common.gui.widgets.IndicatorWidget;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ContainerEngineSteam extends RailcraftContainer {

    private final TileEngineSteam tile;
    private double lastOutput;

    public ContainerEngineSteam(InventoryPlayer inventoryplayer, TileEngineSteam tile) {
        this.tile = tile;

        addWidget(new FluidGaugeWidget(tile.getTankManager().get(0), 71, 23, 176, 0, 16, 47));

        addWidget(new IndicatorWidget(tile.mjIndicator, 94, 25, 176, 47, 6, 43));

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

        crafter.sendWindowProperty(this, 14, (int) Math.round(tile.currentOutput * 100));
    }

    @Override
    public void sendUpdateToClient() {
        super.sendUpdateToClient();

        for (IContainerListener crafter : listeners) {
            if (lastOutput != tile.currentOutput)
                crafter.sendWindowProperty(this, 14, (int) Math.round(tile.currentOutput * 100));
        }

        this.lastOutput = tile.currentOutput;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int value) {
        if (id == 14) {
            tile.currentOutput = value / 100D;
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer entityplayer) {
        return TileRailcraft.isUsableByPlayerHelper(tile, entityplayer);
    }

}
