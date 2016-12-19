/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2016
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.blocks.detector;

import mods.railcraft.common.util.network.RailcraftInputStream;
import mods.railcraft.common.util.network.RailcraftOutputStream;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static mods.railcraft.common.plugins.forge.PowerPlugin.FULL_POWER;
import static mods.railcraft.common.plugins.forge.PowerPlugin.NO_POWER;

public abstract class DetectorEntity<T> extends Detector {
    private final List<Class<? extends T>> entities;
    private Class<? extends T> currentEntity;
    private final Class<? extends T> defaultEntity;

    // TODO: fix for new registry
    protected DetectorEntity(Class<? extends T> classObject, Class<? extends T> defaultEntity) {
        this.defaultEntity = defaultEntity;
//        Set<ResourceLocation> entities = ForgeRegistries.ENTITIES.getKeys();
//        this.entities = entities.stream()
//                .filter(classObject::isAssignableFrom)
//                .<Class<? extends T>>map(e -> e.asSubclass(classObject))
//                .collect(Collectors.toList());
//        currentEntity = this.entities.stream().findAny().orElse(defaultEntity);
        this.entities = Collections.emptyList();
    }

    public Class<? extends T> getCurrentEntity() {
        return currentEntity;
    }

    public Class<? extends T> nextEntity() {
        int index = entities.indexOf(currentEntity);
        currentEntity = entities.get((index + 1) % entities.size());
        return currentEntity;
    }

    public Class<? extends T> previousEntity() {
        int index = entities.indexOf(currentEntity);
        currentEntity = entities.get((index + entities.size() - 1) % entities.size());
        return currentEntity;
    }

    @Override
    public int testCarts(List<EntityMinecart> carts) {
        for (EntityMinecart cart : carts) {
            List<Entity> passengers = cart.getPassengers();
            if (passengers.isEmpty())
                return NO_POWER;
            if (passengers.stream().anyMatch(e -> currentEntity.isAssignableFrom(e.getClass())))
                return FULL_POWER;
        }
        return NO_POWER;
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);

        data.setString("entity", currentEntity.getName());
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);

        String name = data.getString("entity");
        currentEntity = getEntityClass(name);
    }

    private Class<? extends T> getEntityClass(String className) {
        return entities.stream().filter(a -> className.equals(a.getName())).findAny().orElse(defaultEntity);
    }

    @Override
    public void writePacketData(RailcraftOutputStream data) throws IOException {
        super.writePacketData(data);

        writeGuiData(data);
    }

    @Override
    public void readPacketData(RailcraftInputStream data) throws IOException {
        super.readPacketData(data);

        readGuiData(data, null);
    }

    @Override
    public void writeGuiData(RailcraftOutputStream data) throws IOException {
        data.writeUTF(currentEntity.getName());
    }

    @Override
    public void readGuiData(RailcraftInputStream data, @Nullable EntityPlayer sender) throws IOException {
        String name = data.readUTF();
        currentEntity = getEntityClass(name);
    }

}
