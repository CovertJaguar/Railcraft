package mods.railcraft.common.gui.containers;

import mods.railcraft.common.blocks.tracks.outfitted.kits.TrackKitDumping;
import mods.railcraft.common.gui.slots.SlotFilter;
import mods.railcraft.common.gui.slots.SlotMinecartFilter;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;

/**
 *
 */
public class ContainerTrackDumping extends RailcraftContainer {

    public final TrackKitDumping kit;

    protected ContainerTrackDumping(InventoryPlayer playerInv, TrackKitDumping kit) {
        this.kit = kit;

        for (int i = 0; i < 3; i++) {
            addSlot(new SlotMinecartFilter(kit.getCartFilter(), i, 25 + i * 18, 45));
        }

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 3; j++) {
                addSlot(new SlotFilter(kit.getItemFilter(), i * 3 + j, 98 + j * 18, 36 + i * 18));
            }
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlot(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            addSlot(new Slot(playerInv, i, 8 + i * 18, 142));
        }
    }
}
