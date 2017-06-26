/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2017
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/

package mods.railcraft.common.blocks.machine.charge;

import mods.railcraft.common.blocks.machine.ItemMachine;
import mods.railcraft.common.gui.tooltips.ToolTip;
import mods.railcraft.common.plugins.forge.LocalizationPlugin;
import mods.railcraft.common.util.misc.HumanReadableNumberFormatter;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * Created by CovertJaguar on 6/25/2017 for Railcraft.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class ItemChargeBattery extends ItemMachine {
    public ItemChargeBattery(Block block) {
        super(block);
    }

    @Override
    public ToolTip getToolTip(ItemStack stack, EntityPlayer player, boolean adv) {
        ToolTip tips = super.getToolTip(stack, player, adv);
        BatteryVariant variant = (BatteryVariant) getVariant(stack);
        if (variant != null) {
            if (tips == null)
                tips = new ToolTip();
            tips.add(LocalizationPlugin.translate("tile.railcraft.charge.battery.tips.capacity", HumanReadableNumberFormatter.format(variant.capacity)));
            tips.add(LocalizationPlugin.translate("tile.railcraft.charge.battery.tips.maxdraw", HumanReadableNumberFormatter.format(variant.maxDraw)));
            tips.add(LocalizationPlugin.translate("tile.railcraft.charge.battery.tips.loss", HumanReadableNumberFormatter.format(variant.loss)));
            tips.add(LocalizationPlugin.translate("tile.railcraft.charge.battery.tips.efficiency", HumanReadableNumberFormatter.format(variant.efficiency)));
        }
        return tips;
    }

}
