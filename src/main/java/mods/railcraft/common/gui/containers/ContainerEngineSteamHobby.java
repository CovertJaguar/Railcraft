/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2019
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.gui.containers;

import mods.railcraft.common.blocks.single.TileEngineSteamHobby;
import mods.railcraft.common.gui.slots.SlotOutput;
import mods.railcraft.common.gui.slots.SlotRailcraft;
import mods.railcraft.common.gui.widgets.FluidGaugeWidget;
import mods.railcraft.common.gui.widgets.IndicatorWidget;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IContainerListener;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ContainerEngineSteamHobby extends RailcraftContainer {

    private final TileEngineSteamHobby tile;
    private double lastBurnTime;
    private double lastItemBurnTime;
    private double lastOutput;

    public ContainerEngineSteamHobby(InventoryPlayer inventoryplayer, TileEngineSteamHobby tile) {
        super(tile);
        this.tile = tile;

        addWidget(new FluidGaugeWidget(tile.getTankManager().get(0), 17, 23, 176, 0, 16, 47));
        addWidget(new FluidGaugeWidget(tile.getTankManager().get(1), 107, 23, 176, 0, 16, 47));

        addWidget(new IndicatorWidget(tile.boiler.heatIndicator, 40, 25, 176, 61, 6, 43));
        addWidget(new IndicatorWidget(tile.mjIndicator, 94, 25, 182, 61, 6, 43));

        addSlot(new SlotRailcraft(tile, 0, 62, 39)); // Fuel
        addSlot(new SlotRailcraft(tile, 1, 143, 21)); // Water
        addSlot(new SlotOutput(tile, 2, 143, 56));

        addPlayerSlots(inventoryplayer);
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        listener.sendWindowProperty(this, 10, (int) Math.round(tile.boiler.burnTime));
        listener.sendWindowProperty(this, 11, (int) Math.round(tile.boiler.currentItemBurnTime));
        listener.sendWindowProperty(this, 12, (int) Math.round(tile.currentOutput * 100));
    }

    @Override
    public void sendUpdateToClient() {
        super.sendUpdateToClient();

        for (IContainerListener crafter : listeners) {
            if (lastBurnTime != tile.boiler.burnTime)
                crafter.sendWindowProperty(this, 10, (int) Math.round(tile.boiler.burnTime));

            if (lastItemBurnTime != tile.boiler.currentItemBurnTime)
                crafter.sendWindowProperty(this, 11, (int) Math.round(tile.boiler.currentItemBurnTime));

            if (lastOutput != tile.currentOutput)
                crafter.sendWindowProperty(this, 12, (int) Math.round(tile.currentOutput * 100));
        }

        this.lastBurnTime = tile.boiler.burnTime;
        this.lastItemBurnTime = tile.boiler.currentItemBurnTime;
        this.lastOutput = tile.currentOutput;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int value) {

        switch (id) {
            case 10:
                tile.boiler.burnTime = value;
                break;
            case 11:
                tile.boiler.currentItemBurnTime = value;
                break;
            case 12:
                tile.currentOutput = value / 100D;
                break;
        }
    }

}
