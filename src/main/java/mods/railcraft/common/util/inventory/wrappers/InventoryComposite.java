/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2017
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/

package mods.railcraft.common.util.inventory.wrappers;

import com.google.common.collect.ForwardingList;
import mods.railcraft.common.util.inventory.InventoryFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by CovertJaguar on 5/28/2017 for Railcraft.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class InventoryComposite extends ForwardingList<IInventoryObject> implements IInventoryComposite {
    private final List<IInventoryObject> list = new ArrayList<>(10);

    private InventoryComposite(IInventoryObject... objects) {
        addAll(Arrays.asList(objects));
    }

    private InventoryComposite(Collection<IInventoryObject> objects) {
        addAll(objects);
    }

    @Override
    protected List<IInventoryObject> delegate() {
        return list;
    }

    @Override
    public boolean add(IInventoryObject element) {
        //noinspection ConstantConditions
        return element != null && list.add(element);
    }

    @Override
    public void add(int index, IInventoryObject element) {
        //noinspection ConstantConditions
        if (element != null)
            list.add(index, element);
    }

    @Override
    public boolean addAll(Collection<? extends IInventoryObject> collection) {
        return standardAddAll(collection);
    }

    @Override
    public boolean addAll(int index, Collection<? extends IInventoryObject> elements) {
        return standardAddAll(index, elements);
    }

    public static InventoryComposite of(Object... objects) {
        InventoryComposite composite = new InventoryComposite();
        for (Object obj : objects) {
            if (obj instanceof IInventoryComposite) {
                for (IInventoryObject inv : (IInventoryComposite) obj) {
                    composite.add(inv);
                }
            } else {
                IInventoryObject inv = InventoryFactory.get(obj);
                if (inv != null)
                    composite.add(inv);
            }
        }
        return composite;
    }

    public static InventoryComposite of(Collection<IInventoryObject> objects) {
        return objects.stream().filter(Objects::nonNull).collect(Collectors.toCollection(InventoryComposite::new));
    }

    public static InventoryComposite make() {
        return new InventoryComposite();
    }

    @Override
    public Stream<IInventoryObject> stream() {
        return super.stream();
    }
}
