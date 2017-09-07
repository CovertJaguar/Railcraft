/* 
 * Copyright (c) CovertJaguar, 2014 http://railcraft.info
 * 
 * This code is the property of CovertJaguar
 * and may only be used with explicit written
 * permission unless otherwise specified on the
 * license page at http://railcraft.info/wiki/info:license.
 */
package mods.railcraft.common.util.inventory;

import mods.railcraft.api.tracks.ITrackKitInstance;
import mods.railcraft.common.util.inventory.filters.StackFilters;
import net.minecraft.inventory.IInventory;

/**
 * Creates a standalone instance of IInventory of max stacksize = 127.
 *
 * Useful for creating filter inventories.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class PhantomInventory extends StandaloneInventory {

    public PhantomInventory(int size, String name, IInventory callback) {
        super(size, name, callback);
    }

    public PhantomInventory(int size, IInventory callback) {
        super(size, null, callback);
    }

    public PhantomInventory(int size, ITrackKitInstance callback) {
        super(size, null, callback);
    }

    public PhantomInventory(int size, String name) {
        super(size, name);
    }

    public PhantomInventory(int size) {
        super(size, null, (IInventory) null);
    }

    @Override
    protected String invTypeName() {
        return "Phantom";
    }

    @Override
    public int getInventoryStackLimit() {
        return 127;
    }
}
