/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2017
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.client.gui;

import mods.railcraft.common.blocks.machine.equipment.TileRollingMachine;
import mods.railcraft.common.core.RailcraftConstants;
import mods.railcraft.common.gui.containers.ContainerRollingMachine;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.text.translation.I18n;

public class GuiRollingMachine extends TileGui {

    private final TileRollingMachine tile;

    public GuiRollingMachine(InventoryPlayer inventoryplayer, TileRollingMachine tile) {
        this(tile, new ContainerRollingMachine(inventoryplayer, tile), RailcraftConstants.GUI_TEXTURE_FOLDER + "gui_rolling_manual.png");
    }

    protected GuiRollingMachine(TileRollingMachine tile, ContainerRollingMachine container, String texture) {
        super(tile, container, texture);
        this.tile = tile;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        fontRendererObj.drawString(I18n.translateToLocal("container.inventory"), 8, (ySize - 96) + 2, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
        super.drawGuiContainerBackgroundLayer(f, i, j);
        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;
        if (tile.getProgress() > 0) {
            int progress = tile.getProgressScaled(23);
            drawTexturedModalRect(x + 89, y + 45, 176, 0, progress + 1, 12);
        }
    }
}
