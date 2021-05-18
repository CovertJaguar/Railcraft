/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2019
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.gui.widgets;

import mods.railcraft.common.gui.tooltips.ToolTip;
import mods.railcraft.common.gui.tooltips.ToolTipLine;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public abstract class IndicatorController implements IIndicatorController {

    protected final ToolTip tips = new ToolTip() {
        @Override
        public void refresh() {
            refreshToolTip();
        }
    };
    protected final ToolTipLine tip = new ToolTipLine();

    protected IndicatorController() {
        tips.add(tip);
    }

    protected void refreshToolTip() {
    }

    @SideOnly(Side.CLIENT)
    @Override
    public final ToolTip getToolTip() {
        return tips;
    }
}
