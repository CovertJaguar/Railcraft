/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2017
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.blocks.machine.epsilon;

import mods.railcraft.api.core.IRailcraftModule;
import mods.railcraft.common.blocks.IRailcraftBlockContainer;
import mods.railcraft.common.blocks.RailcraftBlocks;
import mods.railcraft.common.blocks.machine.IEnumMachine;
import mods.railcraft.common.blocks.machine.TileMachineBase;
import mods.railcraft.common.gui.tooltips.ToolTip;
import mods.railcraft.common.modules.ModuleCharge;
import mods.railcraft.common.modules.ModuleSteam;
import mods.railcraft.common.modules.RailcraftModuleManager;
import mods.railcraft.common.plugins.forge.LocalizationPlugin;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Tuple;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author CovertJaguar
 */
public enum EnumMachineEpsilon implements IEnumMachine<EnumMachineEpsilon> {

    ADMIN_STEAM_PRODUCER(ModuleSteam.class, "admin.steam.producer", TileAdminSteamProducer.class, 2, 1),
    FORCE_TRACK_EMITTER(ModuleCharge.class, "force.track.emitter", TileForceTrackEmitter.class, 1, 1),
    FLUX_TRANSFORMER(ModuleCharge.class, "flux.transformer", TileFluxTransformer.class, 1, 1),
    ENGRAVING_BENCH("emblem", "engraving.bench", TileEngravingBench.class, 4, 1);
    public static final PropertyEnum<EnumMachineEpsilon> VARIANT = PropertyEnum.create("variant", EnumMachineEpsilon.class);
    public static final EnumMachineEpsilon[] VALUES = values();
    private static final List<EnumMachineEpsilon> creativeList = new ArrayList<EnumMachineEpsilon>();

    static {
        creativeList.add(FLUX_TRANSFORMER);
        creativeList.add(FORCE_TRACK_EMITTER);
        creativeList.add(ADMIN_STEAM_PRODUCER);
        creativeList.add(ENGRAVING_BENCH);
    }

    private final String moduleName;
    private final String tag;
    private final Class<? extends TileMachineBase> tile;
    private final int textureWidth, textureHeight;
    private ToolTip tip;

    EnumMachineEpsilon(String moduleName, String tag, Class<? extends TileMachineBase> tile, int textureWidth, int textureHeight) {
        this.moduleName = moduleName;
        this.tile = tile;
        this.tag = tag;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }

    EnumMachineEpsilon(Class<? extends IRailcraftModule> module, String tag, Class<? extends TileMachineBase> tile, int textureWidth, int textureHeight) {
        this(RailcraftModuleManager.getModuleName(module), tag, tile, textureWidth, textureHeight);
    }

    @Override
    public Definition getDef() {
        return null;
    }

    public static EnumMachineEpsilon fromId(int id) {
        if (id < 0 || id >= VALUES.length)
            id = 0;
        return VALUES[id];
    }

    @Override
    public boolean isDepreciated() {
        return false;
    }

    @Override
    public String getBaseTag() {
        return tag;
    }

    @Override
    public String getTag() {
        return "tile.railcraft.machine.epsilon." + tag;
    }

    @Override
    public boolean passesLight() {
        return false;
    }

    @Override
    public Tuple<Integer, Integer> getTextureDimensions() {
        return new Tuple<>(textureWidth, textureHeight);
    }

    @Override
    public Class<? extends TileMachineBase> getTileClass() {
        return tile;
    }

    @Override
    public TileMachineBase getTileEntity() {
        try {
            return tile.newInstance();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public IRailcraftBlockContainer getContainer() {
        return RailcraftBlocks.MACHINE_EPSILON;
    }

    /**
     * Block is enabled and defined.
     */
    @Override
    public boolean isAvailable() {
        return block() != null && isEnabled();
    }

    @Nullable
    @Override
    public ToolTip getToolTip(ItemStack stack, EntityPlayer player, boolean adv) {
        if (tip != null)
            return tip;
        String tipTag = getLocalizationTag() + ".tips";
        if (LocalizationPlugin.hasTag(tipTag))
            tip = ToolTip.buildToolTip(tipTag);
        return tip;
    }

    @Override
    public String getName() {
        return tag.replace(".", "_");
    }
}
