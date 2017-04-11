/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2017
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.carts;

import mods.railcraft.common.core.RailcraftConfig;
import mods.railcraft.common.core.RailcraftConstants;
import mods.railcraft.common.plugins.forge.LocalizationPlugin;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.List;

/**
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class ItemCartAnchor extends ItemCart {

    public ItemCartAnchor(IRailcraftCartContainer cartType) {
        super(cartType);
    }

    public static long getFuel(ItemStack cart) {
        long fuel = 0;
        NBTTagCompound nbt = cart.getTagCompound();
        if (nbt != null)
            fuel = nbt.getLong("fuel");
        return fuel;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> info, boolean adv) {
        if ((getCartType() == RailcraftCarts.ANCHOR_WORLD && !RailcraftConfig.anchorFuelWorld.isEmpty()) || (getCartType() == RailcraftCarts.ANCHOR_PERSONAL && !RailcraftConfig.anchorFuelPersonal.isEmpty())) {
            long fuel = getFuel(stack);
            double hours = (double) fuel / RailcraftConstants.TICKS_PER_HOUR;
            String format = LocalizationPlugin.translate("gui.railcraft.anchor.fuel.remaining");
            info.add(String.format(format, hours));
        }
    }

}
