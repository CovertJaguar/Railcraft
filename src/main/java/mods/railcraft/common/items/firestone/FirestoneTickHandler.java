/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2016
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.items.firestone;

import mods.railcraft.common.util.inventory.InvTools;
import mods.railcraft.common.util.inventory.iterators.IInvSlot;
import mods.railcraft.common.util.inventory.iterators.InventoryIterator;
import mods.railcraft.common.util.inventory.wrappers.IInventoryObject;
import mods.railcraft.common.util.misc.Game;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class FirestoneTickHandler {

    private int clock;

    @SubscribeEvent
    public void tick(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (Game.isClient(entity.world))
            return;
        clock++;
        if (clock % 4 != 0)
            return;
        if (entity instanceof EntityPlayer && ((EntityPlayer) entity).openContainer != ((EntityPlayer) entity).inventoryContainer)
            return;
        IInventoryObject inv = InvTools.getInventory(entity);
        if (inv != null) {
            for (IInvSlot slot : InventoryIterator.getRailcraft(inv)) {
                ItemStack stack = slot.getStack();
                FirestoneTools.trySpawnFire(entity.world, entity.getPosition(), stack);
            }
        }
    }

}
