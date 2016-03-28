/* 
 * Copyright (c) CovertJaguar, 2014 http://railcraft.info
 * 
 * This code is the property of CovertJaguar
 * and may only be used with explicit written
 * permission unless otherwise specified on the
 * license page at http://railcraft.info/wiki/info:license.
 */
package mods.railcraft.common.blocks.ore;

import mods.railcraft.common.core.RailcraftConfig;
import mods.railcraft.common.plugins.forge.CreativePlugin;
import mods.railcraft.common.plugins.forge.RailcraftRegistry;
import mods.railcraft.common.plugins.forge.WorldPlugin;
import mods.railcraft.common.util.misc.MiscTools;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.Random;

/**
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class BlockWorldLogic extends Block {

    private static BlockWorldLogic instance;

    public static BlockWorldLogic getBlock() {
        return instance;
    }

    public static void registerBlock() {
        if (instance == null && RailcraftConfig.isBlockEnabled("worldlogic")) {
            instance = new BlockWorldLogic();
            RailcraftRegistry.register(instance);
        }
    }

    public BlockWorldLogic() {
        super(Material.rock);
        setRegistryName("railcraft.worldlogic");
        setResistance(6000000.0F);
        setBlockUnbreakable();
        setStepSound(Block.soundTypeStone);
        setCreativeTab(CreativePlugin.RAILCRAFT_TAB);
        disableStats();

        setTickRandomly(true);
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        super.onBlockAdded(world, pos, state);
        world.scheduleBlockUpdate(pos, this, tickRate(world), 0);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        world.scheduleBlockUpdate(pos, this, tickRate(world), 0);
        if (MiscTools.RANDOM.nextInt(32) != 0)
            return;
        BlockOre blockOre = BlockOre.getBlock();
        if (blockOre == null || !EnumOre.SALTPETER.isEnabled() || !RailcraftConfig.isWorldGenEnabled("saltpeter"))
            return;
        int surfaceY = world.getTopSolidOrLiquidBlock(pos).getY() - 2;

        if (surfaceY < 50 || surfaceY > 100)
            return;
        
        BlockPos surface = new BlockPos(pos.getX(), surfaceY, pos.getZ());

        Block block = WorldPlugin.getBlock(world, surface);
        if (block != Blocks.sand)
            return;

        Block above = WorldPlugin.getBlock(world, surface.up());
        if (above != Blocks.sand)
            return;

        Block below = WorldPlugin.getBlock(world, surface.down());
        if (below != Blocks.sand && below != Blocks.sandstone)
            return;

        int airCount = 0;
        for (EnumFacing side : EnumSet.of(EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.EAST, EnumFacing.WEST)) {
            boolean isAir = world.isAirBlock(surface.offset(side));
            if (isAir)
                airCount++;

            if (airCount > 1)
                return;

            if (isAir)
                continue;

            block = WorldPlugin.getBlockState(world, surface.offset(side)).getBlock();
            if (block != Blocks.sand && block != Blocks.sandstone && block != blockOre)
                return;
        }

        IBlockState newState = blockOre.getDefaultState().withProperty(BlockOre.VARIANT, EnumOre.SALTPETER);
        
        world.setBlockState(new BlockPos(pos.getX(), surfaceY, pos.getZ()), newState, 3);
//        System.out.println("saltpeter spawned");
    }

    @Override
    public int tickRate(World world) {
        return 6000;
    }
}
