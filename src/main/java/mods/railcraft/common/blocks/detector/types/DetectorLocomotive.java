/*
 * Copyright (c) CovertJaguar, 2011-2017
 * http://railcraft.info
 *
 * This code is the property of CovertJaguar
 * and may only be used with explicit written
 * permission unless otherwise specified on the
 * license page at http://railcraft.info/wiki/info:license.
 */
/*******************************************************************************
 * Copyright (c) CovertJaguar, 2011-2016
 * http://railcraft.info
 *
 * This code is the property of CovertJaguar
 * and may only be used with explicit written
 * permission unless otherwise specified on the
 * license page at http://railcraft.info/wiki/info:license.
 ******************************************************************************/
package mods.railcraft.common.blocks.detector.types;

import mods.railcraft.common.blocks.detector.DetectorFilter;
import mods.railcraft.common.blocks.detector.EnumDetector;
import mods.railcraft.common.carts.EntityLocomotive;
import mods.railcraft.common.gui.EnumGui;
import mods.railcraft.common.plugins.color.EnumColor;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.List;

import static mods.railcraft.common.plugins.forge.PowerPlugin.FULL_POWER;
import static mods.railcraft.common.plugins.forge.PowerPlugin.NO_POWER;

/**
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public class DetectorLocomotive extends DetectorFilter {

    public DetectorLocomotive() {
        super(2);
    }

    @Override
    public int testCarts(List<EntityMinecart> carts) {
        for (EntityMinecart cart : carts) {
            if (cart instanceof EntityLocomotive) {
                EntityLocomotive loco = (EntityLocomotive) cart;
                ItemStack primary = getFilters().getStackInSlot(0);
                if (primary != null && EnumColor.fromItemStack(primary) != EnumColor.fromDye(loco.getPrimaryColor())) {
                    continue;
                }
                ItemStack secondary = getFilters().getStackInSlot(1);
                if (secondary != null && EnumColor.fromItemStack(secondary) != EnumColor.fromDye(loco.getSecondaryColor())) {
                    continue;
                }
                return FULL_POWER;
            }
        }
        return NO_POWER;
    }

    @Override
    public boolean blockActivated(EntityPlayer player) {
        openGui(EnumGui.DETECTOR_LOCOMOTIVE, player);
        return true;
    }

    @Override
    public EnumDetector getType() {
        return EnumDetector.LOCOMOTIVE;
    }

}
