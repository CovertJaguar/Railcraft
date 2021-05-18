/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2019
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.blocks.single;

import mods.railcraft.client.util.effects.ClientEffects;
import mods.railcraft.common.blocks.interfaces.ITileTank;
import mods.railcraft.common.fluids.FluidTools;
import mods.railcraft.common.fluids.Fluids;
import mods.railcraft.common.fluids.TankManager;
import mods.railcraft.common.fluids.tanks.FilteredTank;
import mods.railcraft.common.gui.EnumGui;
import mods.railcraft.common.gui.GuiHandler;
import mods.railcraft.common.util.misc.MiscTools;
import mods.railcraft.common.util.sounds.RailcraftSoundEvents;
import mods.railcraft.common.util.sounds.SoundHelper;
import mods.railcraft.common.util.steam.ISteamUser;
import mods.railcraft.common.util.steam.SteamConstants;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

/**
 * @author CovertJaguar <http://www.railcraft.info>
 */
public abstract class TileEngineSteam extends TileEngine implements ISteamUser, ITileTank {

    private static final int TANK_CAPACITY = 8 * FluidTools.BUCKET_VOLUME;
    public static final int TANK_STEAM = 0;
    protected final FilteredTank tankSteam = new FilteredTank(TANK_CAPACITY, this).setFilterFluid(Fluids.STEAM);
    protected final TankManager tankManager = new TankManager();
    private int steamUsed;

    protected TileEngineSteam() {
        tankManager.add(tankSteam);
    }

    @Override
    protected void playSoundOut() {
        SoundHelper.playSoundClient(world, getPos(), RailcraftSoundEvents.MECHANICAL_STEAM_BURST, SoundCategory.BLOCKS, 0.15F, (float) (0.5F + MiscTools.RANDOM.nextGaussian() * 0.1));
    }

    @Override
    protected void playSoundIn() {
        SoundHelper.playSoundClient(world, getPos(), RailcraftSoundEvents.MECHANICAL_STEAM_BURST, SoundCategory.BLOCKS, 0.15F, (float) (1 + MiscTools.RANDOM.nextGaussian() * 0.1));
    }

    private int getParticleRate() {
        switch (getEnergyStage()) {
            case BLUE:
                return 1;
            case GREEN:
                return 2;
            case YELLOW:
                return 3;
            case ORANGE:
                return 4;
            case RED:
                return 5;
            case OVERHEAT:
                return 8;
            default:
                return 0;
        }
    }

    @Override
    public boolean openGui(EntityPlayer player) {
        GuiHandler.openGui(EnumGui.ENGINE_STEAM, player, world, getPos());
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(Random rand) {
        if (isActive() || getEnergyStage() == EnergyStage.OVERHEAT) {
            int steamRate = getParticleRate();
            for (int i = 0; i < steamRate; i++) {
                ClientEffects.INSTANCE.steamEffect(world, this, 0);
            }
        }
    }

    @Override
    public void burn() {
        long output = 0;

        if (getEnergyStage() != EnergyStage.OVERHEAT) {
            if (isPowered()) {
                FluidStack steam = tankSteam.getFluid();
                if (steam != null && steam.amount >= tankSteam.getCapacity() / 2 - SteamConstants.STEAM_PER_UNIT_WATER) {
                    steam = tankManager.drain(0, steamUsedPerTick() - 1, true);
                    if (steam != null)
                        steamUsed += steam.amount;
                }
            }
            FluidStack steam = tankManager.drain(0, 1, true);
            if (steam != null)
                steamUsed += steam.amount;

            if (isPowered()) {
                if (steamUsed >= steamUsedPerTick()) {
                    steamUsed -= steamUsedPerTick();
                    output = getMaxOutputMJ();
                    addEnergy(output);
                }
            } else {
                steamUsed = 0;
                ventSteam();
            }
        }

        currentOutput = (currentOutput * 74D + output) / 75D;
    }

    @Override
    protected void overheat() {
        super.overheat();
        ventSteam();
    }

    protected void ventSteam() {
        getTankManager().drain(TANK_STEAM, 5, true);
    }

    public abstract long getMaxOutputMJ();

    public abstract int steamUsedPerTick();

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);

        tankManager.writeTanksToNBT(data);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);

        tankManager.readTanksFromNBT(data);
    }

    @Override

    public TankManager getTankManager() {
        return tankManager;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return getFacing() != facing && capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (getFacing() != facing && capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
            return (T) getTankManager();
        return super.getCapability(capability, facing);
    }
}
