/*
 * Copyright (c) CovertJaguar, 2011-2017
 * http://railcraft.info
 *
 * This code is the property of CovertJaguar
 * and may only be used with explicit written
 * permission unless otherwise specified on the
 * license page at http://railcraft.info/wiki/info:license.
 */
package mods.railcraft.common.blocks.aesthetics.generic;

import mods.railcraft.api.core.IVariantEnum;
import mods.railcraft.common.blocks.BlockRailcraftSubtyped;
import mods.railcraft.common.blocks.RailcraftBlocks;
import mods.railcraft.common.blocks.machine.RailcraftBlockMetadata;
import mods.railcraft.common.carts.EntityTunnelBore;
import mods.railcraft.common.plugins.forestry.ForestryPlugin;
import mods.railcraft.common.plugins.forge.CreativePlugin;
import mods.railcraft.common.plugins.forge.HarvestPlugin;
import mods.railcraft.common.plugins.forge.WorldPlugin;
import mods.railcraft.common.plugins.misc.MicroBlockPlugin;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

@RailcraftBlockMetadata(variant = EnumGeneric.class)
public class BlockGeneric extends BlockRailcraftSubtyped<EnumGeneric> {


    public BlockGeneric() {
        super(Material.ROCK);
        setDefaultState(blockState.getBaseState().withProperty(getVariantProperty(), EnumGeneric.BLOCK_COKE));
        setResistance(20);
        setHardness(5);
        setSoundType(SoundType.STONE);

        setCreativeTab(CreativePlugin.RAILCRAFT_TAB);
    }

    @Override
    public void initializeDefinintion() {
        HarvestPlugin.setStateHarvestLevel("pickaxe", 1, EnumGeneric.BLOCK_COKE);
        HarvestPlugin.setStateHarvestLevel("pickaxe", 1, EnumGeneric.STONE_ABYSSAL);
        HarvestPlugin.setStateHarvestLevel("pickaxe", 1, EnumGeneric.STONE_QUARRIED);
        HarvestPlugin.setStateHarvestLevel("pickaxe", 1, EnumGeneric.BLOCK_CONCRETE);

        HarvestPlugin.setStateHarvestLevel("pickaxe", 2, EnumGeneric.BLOCK_STEEL);
        HarvestPlugin.setStateHarvestLevel("pickaxe", 2, EnumGeneric.BLOCK_SILVER);
        HarvestPlugin.setStateHarvestLevel("pickaxe", 1, EnumGeneric.BLOCK_LEAD);
        HarvestPlugin.setStateHarvestLevel("pickaxe", 1, EnumGeneric.BLOCK_TIN);
        HarvestPlugin.setStateHarvestLevel("pickaxe", 1, EnumGeneric.BLOCK_COPPER);

        HarvestPlugin.setStateHarvestLevel("axe", 0, EnumGeneric.BLOCK_CREOSOTE);
        HarvestPlugin.setStateHarvestLevel("shovel", 3, EnumGeneric.CRUSHED_OBSIDIAN);

        EntityTunnelBore.addMineableBlock(this);

        ForestryPlugin.addBackpackItem("forestry.miner", EnumGeneric.BLOCK_COKE.getStack());
        ForestryPlugin.addBackpackItem("forestry.miner", EnumGeneric.BLOCK_COPPER.getStack());
        ForestryPlugin.addBackpackItem("forestry.miner", EnumGeneric.BLOCK_LEAD.getStack());
        ForestryPlugin.addBackpackItem("forestry.miner", EnumGeneric.BLOCK_STEEL.getStack());
        ForestryPlugin.addBackpackItem("forestry.miner", EnumGeneric.BLOCK_TIN.getStack());
        ForestryPlugin.addBackpackItem("forestry.miner", EnumGeneric.BLOCK_SILVER.getStack());

        ForestryPlugin.addBackpackItem("forestry.builder", EnumGeneric.BLOCK_CONCRETE.getStack());
        ForestryPlugin.addBackpackItem("forestry.builder", EnumGeneric.BLOCK_CREOSOTE.getStack());

        ForestryPlugin.addBackpackItem("forestry.digger", EnumGeneric.STONE_ABYSSAL.getStack());
        ForestryPlugin.addBackpackItem("forestry.digger", EnumGeneric.STONE_QUARRIED.getStack());

        for (EnumGeneric block : EnumGeneric.VALUES) {
            MicroBlockPlugin.addMicroBlockCandidate(this, block.ordinal());
        }
    }

    @Override
    public IBlockState getState(@Nullable IVariantEnum variant) {
        IBlockState state = getDefaultState();
        if (variant != null) {
            checkVariant(variant);
            state = state.withProperty(getVariantProperty(), (EnumGeneric) variant);
        }
        return state;
    }

    @Nullable
    public static BlockGeneric getBlock() {
        return (BlockGeneric) RailcraftBlocks.GENERIC.block();
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(getVariantProperty(), EnumGeneric.fromOrdinal(meta));
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(getVariantProperty()).ordinal();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, getVariantProperty());
    }

    private EnumGeneric getVariant(IBlockAccess world, BlockPos pos) {
        return getVariant(WorldPlugin.getBlockState(world, pos));
    }

    @Override
    public float getBlockHardness(IBlockState blockState, World worldIn, BlockPos pos) {
        return getVariant(blockState).getHardness();
    }

    @Override
    public int damageDropped(IBlockState state) {
        return state.getValue(getVariantProperty()).ordinal();
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block neighborBlock) {
        getVariant(worldIn, pos).getBlockDef().onNeighborBlockChange(worldIn, pos, state, neighborBlock);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        getVariant(world, pos).getBlockDef().updateTick(world, pos, rand);
    }

    @Override
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        getVariant(stateIn).getBlockDef().randomDisplayTick(worldIn, pos, rand);
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        getVariant(world, pos).getBlockDef().onBlockAdded(world, pos);
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        return getVariant(world, pos).getBlockDef().removedByPlayer(world, player, pos);
    }

    @Override
    public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, EntityLiving.SpawnPlacementType type) {
        return getVariant(world, pos).getBlockDef().canCreatureSpawn(type, world, pos);
    }

    @Override
    public void getSubBlocks(Item item, CreativeTabs tab, List<ItemStack> list) {
        for (EnumGeneric type : EnumGeneric.getCreativeList()) {
            if (type.isEnabled())
                list.add(type.getStack());
        }
    }

    @Override
    public float getExplosionResistance(World world, BlockPos pos, Entity exploder, Explosion explosion) {
        return getVariant(world, pos).getResistance() * 3f / 5f;
    }

    @Override
    public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return getVariant(world, pos).getBlockDef().getFireSpreadSpeed(world, pos, face);
    }

    @Override
    public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return getVariant(world, pos).getBlockDef().getFlammability(world, pos, face);
    }

    @Override
    public boolean isFlammable(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return getVariant(world, pos).getBlockDef().isFlammable(world, pos, face);
    }

    @Override
    public SoundType getSoundType(IBlockState state, World world, BlockPos pos, @Nullable Entity entity) {
        switch (getVariant(state)) {
            case BLOCK_COPPER:
            case BLOCK_LEAD:
            case BLOCK_STEEL:
            case BLOCK_TIN:
            case BLOCK_SILVER:
                return SoundType.METAL;
            case BLOCK_CREOSOTE:
                return SoundType.WOOD;
            case CRUSHED_OBSIDIAN:
            case BLOCK_COKE:
                return SoundType.GROUND;
        }
        return super.getSoundType(state, world, pos, entity);
    }
}
