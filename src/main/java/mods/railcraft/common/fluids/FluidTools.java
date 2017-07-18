/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2017
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.fluids;

import mods.railcraft.client.particles.ParticleDrip;
import mods.railcraft.common.fluids.tanks.StandardTank;
import mods.railcraft.common.plugins.forge.WorldPlugin;
import mods.railcraft.common.util.inventory.InvTools;
import mods.railcraft.common.util.inventory.wrappers.InventoryMapper;
import mods.railcraft.common.util.misc.Game;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.Particle;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.FluidContainerRegistry.FluidContainerData;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

import static mods.railcraft.common.util.inventory.InvTools.isEmpty;

/**
 * @author CovertJaguar <http://www.railcraft.info>
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public final class FluidTools {
    public static final int BUCKET_FILL_TIME = 8;
    public static final int NETWORK_UPDATE_INTERVAL = 128;
    public static final int BUCKET_VOLUME = 1000;
    public static final int PROCESS_VOLUME = BUCKET_VOLUME * 4;
    private static final List<FluidRegistrar> adapters = new ArrayList<FluidRegistrar>();

    static {
        adapters.add(ForgeFluidRegistrar.INSTANCE);
    }

    private FluidTools() {
    }

    public static String toString(@Nullable FluidStack fluidStack) {
        if (fluidStack == null)
            return "null";
        return fluidStack.amount + "x" + fluidStack.getFluid().getName();
    }

    /**
     * Forge is too picking here. So no {@link InvTools#isEmpty(ItemStack)} here.
     *
     * @param stack The stack to check
     * @return True if the liquid is successfully consumed
     */
    public static boolean isSuccessfullyConsumed(@Nullable ItemStack stack) {
        return stack != null && stack.stackSize == 0;
    }

    /**
     * Forge is too picking here. So no {@link InvTools#isEmpty(ItemStack)} here.
     *
     * @param stack The stack to check
     * @return True if the liquid failed to drain/fill
     */
    public static boolean isFailed(@Nullable ItemStack stack) {
        return stack == null;
    }

    @Nullable
    public static net.minecraftforge.fluids.capability.IFluidHandler getFluidHandler(@Nullable EnumFacing side, ICapabilityProvider object) {
        return object.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side);
    }

    public static boolean hasFluidHandler(@Nullable EnumFacing side, ICapabilityProvider object) {
        return object.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side);
    }

    public static boolean interactWithFluidHandler(@Nullable ItemStack heldItem, @Nullable IFluidHandler fluidHandler, EntityPlayer player) {
        if (Game.isHost(player.worldObj))
            return FluidUtil.interactWithFluidHandler(heldItem, fluidHandler, player);
        return FluidItemHelper.isContainer(heldItem);
    }

    public enum ProcessState {
        FILLING,
        DRAINING,
        RESET
    }

    private static void sendToProcessing(IInventory inv) {
        InvTools.moveOneItem(InventoryMapper.make(inv, 0, 1), InventoryMapper.make(inv, 1, 1, false));
    }

    private static void sendToOutput(IInventory inv) {
        InvTools.moveOneItem(InventoryMapper.make(inv, 1, 1), InventoryMapper.make(inv, 2, 1, false));
    }

    private static ProcessState tryFill(IInventory inv, StandardTank tank, ItemStack container) {
        ItemStack filled = FluidUtil.tryFillContainer(container, tank, Fluid.BUCKET_VOLUME, null, true);
        if (isFailed(filled)) {
            sendToOutput(inv);
            return ProcessState.RESET;
        }
        inv.setInventorySlotContents(1, InvTools.makeSafe(filled));
        return ProcessState.FILLING;
    }

    private static ProcessState tryDrain(IInventory inv, StandardTank tank, ItemStack container) {
        ItemStack drained = FluidUtil.tryEmptyContainer(container, tank, Fluid.BUCKET_VOLUME, null, true);
        if (isFailed(drained)) {
            sendToOutput(inv);
            return ProcessState.RESET;
        }
        inv.setInventorySlotContents(1, InvTools.makeSafe(drained));
        return ProcessState.DRAINING;
    }

    /**
     * Expects a three slot inventory, with input as slot 0, processing as slot 1, and output as slot 2.
     * Will handle moving an item through all stages from input to output for either filling or draining.
     */
    public static ProcessState processContainer(IInventory inv, StandardTank tank, boolean defaultToFill, ProcessState state) {
        ItemStack container = inv.getStackInSlot(1);
        if (isEmpty(container) || FluidUtil.getFluidHandler(container) == null) {
            sendToProcessing(inv);
            return ProcessState.RESET;
        }
        if (state == ProcessState.RESET) {
            if (defaultToFill) {
                return tryFill(inv, tank, container);
            } else {
                return tryDrain(inv, tank, container);
            }
        }
        if (state == ProcessState.FILLING)
            return tryFill(inv, tank, container);
        if (state == ProcessState.DRAINING)
            return tryDrain(inv, tank, container);
        return state;
    }
//    @Deprecated
//    public static boolean handleRightClick(IFluidHandler tank, @Nullable EnumFacing side, @Nullable EntityPlayer player, boolean fill, boolean drain) {
//        if (player == null)
//            return false;
//        ItemStack current = player.inventory.getCurrentItem();
//        if (current != null) {
//
//            FluidItemHelper.DrainReturn drainReturn = FluidItemHelper.drainContainer(current, PROCESS_VOLUME);
//
//            if (fill && drainReturn.fluidDrained != null) {
//                int used = tank.fill(side, drainReturn.fluidDrained, false);
//
//                if (used > 0) {
//                    drainReturn = FluidItemHelper.drainContainer(current, used);
//                    if (!player.capabilities.isCreativeMode) {
//                        if (current.stackSize > 1) {
//                            if (drainReturn.container != null && !player.inventory.addItemStackToInventory(drainReturn.container))
//                                return false;
//                            player.inventory.setInventorySlotContents(player.inventory.currentItem, InvTools.depleteItem(current));
//                        } else {
//                            player.inventory.setInventorySlotContents(player.inventory.currentItem, drainReturn.container);
//                        }
//                        player.inventory.markDirty();
//                    }
//                    tank.fill(side, drainReturn.fluidDrained, true);
//                    return true;
//                }
//            } else if (drain) {
//
//                FluidStack available = tank.drain(side, PROCESS_VOLUME, false);
//                if (available != null) {
//                    FluidItemHelper.FillReturn fillReturn = FluidItemHelper.fillContainer(current, available);
//                    if (fillReturn.amount > 0) {
//                        if (current.stackSize > 1) {
//                            if (fillReturn.container != null && !player.inventory.addItemStackToInventory(fillReturn.container))
//                                return false;
//                            player.inventory.setInventorySlotContents(player.inventory.currentItem, InvTools.depleteItem(current));
//                        } else {
//                            player.inventory.setInventorySlotContents(player.inventory.currentItem, fillReturn.container);
//                        }
//                        player.inventory.markDirty();
//                        tank.drain(side, fillReturn.amount, true);
//                        return true;
//                    }
//                }
//            }
//        }
//        return false;
//    }
//

    /**
     * Process containers in input/output slot like tha in the tank cart.
     *
     * @param tank       Fluid tank
     * @param inv        The inventory that contains input/output slots
     * @param inputSlot  The input slot number
     * @param outputSlot The output slot number
     * @return {@code true} if changes have been done to the tank
     */
    public static boolean processContainers(StandardTank tank, IInventory inv, int inputSlot, int outputSlot) {
        return processContainers(tank, inv, inputSlot, outputSlot, tank.getFluidType(), true, true);
    }

    public static boolean processContainers(StandardTank tank, IInventory inv, int inputSlot, int outputSlot, @Nullable Fluid fluidToFill, boolean processFilled, boolean processEmpty) {
        TankManager tankManger = new TankManager();
        tankManger.add(tank);
        return processContainers(tankManger, inv, inputSlot, outputSlot, fluidToFill, processFilled, processEmpty);
    }

    public static boolean processContainers(TankManager tank, IInventory inv, int inputSlot, int outputSlot, @Nullable Fluid fluidToFill) {
        return processContainers(tank, inv, inputSlot, outputSlot, fluidToFill, true, true);
    }

    public static boolean processContainers(IFluidHandler fluidHandler, IInventory inv, int inputSlot, int outputSlot, @Nullable Fluid fluidToFill, boolean processFilled, boolean processEmpty) {
        ItemStack input = inv.getStackInSlot(inputSlot);

        if (isEmpty(input))
            return false;

        if (processFilled && drainContainers(fluidHandler, inv, inputSlot, outputSlot))
            return true;

        if (processEmpty && fluidToFill != null)
            return fillContainers(fluidHandler, inv, inputSlot, outputSlot, fluidToFill);
        return false;
    }

    public static boolean fillContainers(IFluidHandler source, IInventory inv, int inputSlot, int outputSlot, @Nullable Fluid fluidToFill) {
        ItemStack input = inv.getStackInSlot(inputSlot);
        //need an empty container
        if (isEmpty(input))
            return false;
        ItemStack output = inv.getStackInSlot(outputSlot);
        ItemStack container = FluidUtil.tryFillContainer(input, source, BUCKET_VOLUME, null, false);
        //check failure
        if (isFailed(container))
            return false;
        //check filled fluid type
        if (fluidToFill != null && !isEmpty(container)) {
            FluidStack fluidStack = FluidUtil.getFluidContained(container);
            if (fluidStack != null && fluidStack.getFluid() != fluidToFill)
                return false;
        }
        //check place for container
        if (!hasPlaceToPutContainer(output, container))
            return false;
        //do actual things here
        container = FluidUtil.tryFillContainer(input, source, BUCKET_VOLUME, null, true);
        storeContainer(inv, inputSlot, outputSlot, container);
        return true;
    }

    public static boolean drainContainers(IFluidHandler dest, IInventory inv, int inputSlot, int outputSlot) {
        ItemStack input = inv.getStackInSlot(inputSlot);
        //need a valid container
        if (isEmpty(input))
            return false;
        ItemStack output = inv.getStackInSlot(outputSlot);
        ItemStack container = FluidUtil.tryEmptyContainer(input, dest, BUCKET_VOLUME, null, false);
        //check failure
        if (isFailed(container))
            return false;
        //check place for container
        if (!hasPlaceToPutContainer(output, container))
            return false;
        //do actual things here
        container = FluidUtil.tryEmptyContainer(input, dest, BUCKET_VOLUME, null, true);
        storeContainer(inv, inputSlot, outputSlot, container);
        return true;
    }

    private static boolean hasPlaceToPutContainer(@Nullable ItemStack output, @Nullable ItemStack container) {
        return isEmpty(output) || isEmpty(container) || output.stackSize < output.getMaxStackSize() && InvTools.isItemEqual(container, output);
    }

    /**
     * We can assume that if null is passed for the container that the container
     * was consumed by the process and we should just remove the input container.
     */
    private static void storeContainer(IInventory inv, int inputSlot, int outputSlot, @Nullable ItemStack container) {
        if (isEmpty(container)) {
            inv.decrStackSize(inputSlot, 1);
            return;
        }
        ItemStack output = inv.getStackInSlot(outputSlot);
        if (isEmpty(output))
            inv.setInventorySlotContents(outputSlot, container);
        else
            output.stackSize++;
        inv.decrStackSize(inputSlot, 1);
    }

    public static boolean registerBucket(FluidStack liquid, ItemStack filled) {
        ItemStack empty = new ItemStack(Items.BUCKET);
        return registerContainer(liquid, filled, empty);
    }

    public static boolean registerBottle(FluidStack liquid, ItemStack filled) {
        ItemStack empty = new ItemStack(Items.GLASS_BOTTLE);
        return registerContainer(liquid, filled, empty);
    }

    public static boolean registerContainer(FluidStack fluidStack, ItemStack filled, @Nullable ItemStack empty) {
        if (!isEmpty(empty)) {
            FluidContainerData container = new FluidContainerData(fluidStack, filled, empty);
            registerContainer(container);
            return true;
        }
        return false;
    }

    public static void registerContainer(FluidContainerData container) {
        for (FluidRegistrar adapter : adapters) {
            adapter.registerContainer(container);
        }
    }

    public static Collection<ItemStack> getContainersFilledWith(FluidStack fluidStack) {
        List<ItemStack> containers = new ArrayList<ItemStack>();
        for (FluidContainerData data : FluidContainerRegistry.getRegisteredFluidContainerData()) {
            FluidStack inContainer = FluidItemHelper.getFluidStackInContainer(data.filledContainer);
            if (inContainer != null && inContainer.containsFluid(fluidStack))
                containers.add(data.filledContainer.copy());
        }
        return containers;
    }

    public static void nerfWaterBottle() {
        for (FluidContainerData data : FluidContainerRegistry.getRegisteredFluidContainerData()) {
            if (data.filledContainer.getItem() == Items.POTIONITEM && data.emptyContainer.getItem() == Items.GLASS_BOTTLE && Fluids.WATER.is(data.fluid)) {
                data.fluid.amount = 333;
                return;
            }
        }
    }

    @Nullable
    public static FluidStack drainBlock(World world, BlockPos pos, boolean doDrain) {
        return drainBlock(WorldPlugin.getBlockState(world, pos), world, pos, doDrain);
    }

    @Nullable
    public static FluidStack drainBlock(IBlockState state, World world, BlockPos pos, boolean doDrain) {
        FluidStack fluid;
        if ((fluid = drainForgeFluid(state, world, pos, doDrain)) != null)
            return fluid;
        else if ((fluid = drainVanillaFluid(state, world, pos, doDrain, Fluids.WATER, Blocks.WATER, Blocks.FLOWING_WATER)) != null)
            return fluid;
        else if ((fluid = drainVanillaFluid(state, world, pos, doDrain, Fluids.LAVA, Blocks.LAVA, Blocks.FLOWING_LAVA)) != null)
            return fluid;
        return null;
    }

    @Nullable
    private static FluidStack drainForgeFluid(IBlockState state, World world, BlockPos pos, boolean doDrain) {
        if (state.getBlock() instanceof IFluidBlock) {
            IFluidBlock fluidBlock = (IFluidBlock) state.getBlock();
            if (fluidBlock.canDrain(world, pos))
                return fluidBlock.drain(world, pos, doDrain);
        }
        return null;
    }

    @Nullable
    private static FluidStack drainVanillaFluid(IBlockState state, World world, BlockPos pos, boolean doDrain, Fluids fluid, Block... blocks) {
        boolean matches = false;
        for (Block block : blocks) {
            if (state.getBlock() == block)
                matches = true;
        }
        if (!matches)
            return null;
        if (!(state.getBlock() instanceof BlockLiquid))
            return null;
        int level = state.getValue(BlockLiquid.LEVEL);
        if (level != 0)
            return null;
        if (doDrain)
            WorldPlugin.isBlockAir(world, pos);
        return fluid.getBucket();
    }

    public static boolean isFullFluidBlock(World world, BlockPos pos) {
        return isFullFluidBlock(WorldPlugin.getBlockState(world, pos), world, pos);
    }

    @SuppressWarnings("SimplifiableIfStatement")
    public static boolean isFullFluidBlock(IBlockState state, World world, BlockPos pos) {
        if (state.getBlock() instanceof BlockLiquid)
            return state.getValue(BlockLiquid.LEVEL) == 0;
        if (state.getBlock() instanceof IFluidBlock)
            return Math.abs(((IFluidBlock) state.getBlock()).getFilledPercentage(world, pos)) == 1.0;
        return false;
    }

    @Nullable
    public static Fluid getFluid(Block block) {
        if (block instanceof IFluidBlock)
            return ((IFluidBlock) block).getFluid();
        else if (block == Blocks.WATER || block == Blocks.FLOWING_WATER)
            return FluidRegistry.WATER;
        else if (block == Blocks.LAVA || block == Blocks.FLOWING_LAVA)
            return FluidRegistry.LAVA;
        return null;
    }

    @Nullable
    public static Fluid getFluid(IBlockState state) {
        return getFluid(state.getBlock());
    }

    public static int getFluidId(@Nullable FluidStack stack) {
        if (stack == null)
            return -1;
        if (stack.getFluid() == null)
            return -1;
        return FluidRegistry.getFluidID(stack.getFluid().getName());
    }

    @SideOnly(Side.CLIENT)
    public static void drip(World world, BlockPos pos, IBlockState state, Random rand, float particleRed, float particleGreen, float particleBlue) {
        if (rand.nextInt(10) == 0 && world.isSideSolid(pos.down(), EnumFacing.UP) && !WorldPlugin.getBlockMaterial(world, pos.down(2)).blocksMovement()) {
            double px = (double) ((float) pos.getX() + rand.nextFloat());
            double py = (double) pos.getY() - 1.05D;
            double pz = (double) ((float) pos.getZ() + rand.nextFloat());

            Particle fx = new ParticleDrip(world, new Vec3d(px, py, pz), particleRed, particleGreen, particleBlue);
            FMLClientHandler.instance().getClient().effectRenderer.addEffect(fx);
        }
    }

    public static boolean testProperties(boolean all, @Nullable IFluidHandler fluidHandler, Predicate<IFluidTankProperties> test) {
        if (fluidHandler == null)
            return false;
        IFluidTankProperties[] properties = fluidHandler.getTankProperties();
        if (all)
            return Arrays.stream(properties).allMatch(test);
        return Arrays.stream(properties).anyMatch(test);
    }
}
