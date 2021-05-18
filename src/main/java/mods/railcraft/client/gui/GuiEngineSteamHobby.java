/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2019
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.client.gui;

import buildcraft.api.mj.MjAPI;
import mods.railcraft.common.blocks.single.TileEngineSteamHobby;
import mods.railcraft.common.gui.containers.ContainerEngineSteamHobby;
import mods.railcraft.common.plugins.buildcraft.power.MjPlugin;
import net.minecraft.entity.player.InventoryPlayer;

public class GuiEngineSteamHobby extends GuiTitled {
    private static final String OUTPUT = "%s MJ";
    private final TileEngineSteamHobby tile;

    public GuiEngineSteamHobby(InventoryPlayer inv, TileEngineSteamHobby tile) {
        super(tile, new ContainerEngineSteamHobby(inv, tile), "gui_engine_hobby.png");
        this.tile = tile;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        fontRenderer.drawString(String.format(OUTPUT, MjPlugin.FORMAT.format(tile.currentOutput / (double) MjPlugin.MJ)), 55, 60, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
        super.drawGuiContainerBackgroundLayer(par1, par3, par3);
        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;

        if (tile.boiler.hasFuel()) {
            int scale = tile.boiler.getBurnProgressScaled(12);
            drawTexturedModalRect(x + 62, y + 34 - scale, 176, 59 - scale, 14, scale + 2);
        }
    }

}
