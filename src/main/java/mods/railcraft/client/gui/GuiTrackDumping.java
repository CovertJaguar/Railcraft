package mods.railcraft.client.gui;

import mods.railcraft.common.blocks.RailcraftTileEntity;
import mods.railcraft.common.core.RailcraftConstants;
import mods.railcraft.common.gui.containers.ContainerTrackDumping;
import mods.railcraft.common.plugins.forge.LocalizationPlugin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * GUI for dumping track kit.
 */
@SideOnly(Side.CLIENT)
public class GuiTrackDumping extends TileGui {

    private static final String LOCATION = RailcraftConstants.GUI_TEXTURE_FOLDER + "gui_track_dumping.png";
    private final String FILTER_LABEL = LocalizationPlugin.translate("gui.railcraft.filters");
    private final String CART_FILTER_LABEL = LocalizationPlugin.translate("gui.railcraft.filters.carts");

    public GuiTrackDumping(ContainerTrackDumping container) {
        super((RailcraftTileEntity) container.kit.getTile(), container, LOCATION);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        FontRenderer renderer = Minecraft.getMinecraft().fontRendererObj;
        GuiTools.drawStringCenteredAtPos(renderer, FILTER_LABEL, 123, 26);
        GuiTools.drawStringCenteredAtPos(renderer, CART_FILTER_LABEL, 51, 35);
        fontRendererObj.drawString(LocalizationPlugin.translate("container.inventory"), 8, (ySize - 96) + 2, 0x404040);
    }
}
