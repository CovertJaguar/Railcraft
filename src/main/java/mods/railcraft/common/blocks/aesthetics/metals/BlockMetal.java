/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2019
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.blocks.aesthetics.metals;

import mods.railcraft.api.core.IVariantEnum;
import mods.railcraft.common.blocks.BlockMeta;
import mods.railcraft.common.blocks.BlockRailcraftSubtyped;
import mods.railcraft.common.blocks.RailcraftBlocks;
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
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

@BlockMeta.Variant(EnumMetal.class)
public class BlockMetal extends BlockRailcraftSubtyped<EnumMetal> {

    public BlockMetal() {
        super(Material.IRON);
        setDefaultState(blockState.getBaseState().withProperty(getVariantProperty(), EnumMetal.BLOCK_COPPER));
        setResistance(20);
        setHardness(5);
        setSoundType(SoundType.METAL);

        setCreativeTab(CreativePlugin.RAILCRAFT_TAB);
    }

    @Override
    public void initializeDefinition() {
        HarvestPlugin.setStateHarvestLevel("pickaxe", 2, EnumMetal.BLOCK_STEEL);
        HarvestPlugin.setStateHarvestLevel("pickaxe", 2, EnumMetal.BLOCK_SILVER);
        HarvestPlugin.setStateHarvestLevel("pickaxe", 1, EnumMetal.BLOCK_LEAD);
        HarvestPlugin.setStateHarvestLevel("pickaxe", 1, EnumMetal.BLOCK_TIN);
        HarvestPlugin.setStateHarvestLevel("pickaxe", 1, EnumMetal.BLOCK_COPPER);
        HarvestPlugin.setStateHarvestLevel("pickaxe", 1, EnumMetal.BLOCK_BRONZE);
        HarvestPlugin.setStateHarvestLevel("pickaxe", 1, EnumMetal.BLOCK_NICKEL);
        HarvestPlugin.setStateHarvestLevel("pickaxe", 1, EnumMetal.BLOCK_INVAR);

        EntityTunnelBore.addMineableBlock(this);

        ForestryPlugin.addBackpackItem("forestry.miner", EnumMetal.BLOCK_COPPER.getStack());
        ForestryPlugin.addBackpackItem("forestry.miner", EnumMetal.BLOCK_LEAD.getStack());
        ForestryPlugin.addBackpackItem("forestry.miner", EnumMetal.BLOCK_STEEL.getStack());
        ForestryPlugin.addBackpackItem("forestry.miner", EnumMetal.BLOCK_TIN.getStack());
        ForestryPlugin.addBackpackItem("forestry.miner", EnumMetal.BLOCK_SILVER.getStack());
        ForestryPlugin.addBackpackItem("forestry.miner", EnumMetal.BLOCK_BRONZE.getStack());
        ForestryPlugin.addBackpackItem("forestry.miner", EnumMetal.BLOCK_NICKEL.getStack());
        ForestryPlugin.addBackpackItem("forestry.miner", EnumMetal.BLOCK_INVAR.getStack());

        for (EnumMetal block : EnumMetal.VALUES) {
            MicroBlockPlugin.addMicroBlockCandidate(this, block.ordinal());
        }
    }

    @Override
    public IBlockState getState(@Nullable IVariantEnum variant) {
        IBlockState state = getDefaultState();
        if (variant != null) {
            checkVariant(variant);
            state = state.withProperty(getVariantProperty(), (EnumMetal) variant);
        }
        return state;
    }

    public static BlockMetal getBlock() {
        return (BlockMetal) RailcraftBlocks.METAL.block();
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(getVariantProperty(), EnumMetal.fromOrdinal(meta));
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

    private EnumMetal getVariant(IBlockAccess world, BlockPos pos) {
        return getVariant(WorldPlugin.getBlockState(world, pos));
    }

    @SuppressWarnings("deprecation")
    @Override
    public float getBlockHardness(IBlockState blockState, World worldIn, BlockPos pos) {
        return getVariant(blockState).getHardness();
    }

    @Override
    public int damageDropped(IBlockState state) {
        return state.getValue(getVariantProperty()).ordinal();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block neighborBlock, BlockPos neighborPos) {
        getVariant(state).getBlockDef().onNeighborBlockChange(worldIn, pos, state, neighborBlock);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        getVariant(state).getBlockDef().updateTick(world, pos, rand);
    }

    @Override
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        getVariant(stateIn).getBlockDef().randomDisplayTick(worldIn, pos, rand);
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        getVariant(state).getBlockDef().onBlockAdded(world, pos);
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        return getVariant(state).getBlockDef().removedByPlayer(world, player, pos);
    }

    @Override
    public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, EntityLiving.SpawnPlacementType type) {
        return getVariant(state).getBlockDef().canCreatureSpawn(type, world, pos);
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
        for (EnumMetal type : EnumMetal.getCreativeList()) {
            if (type.isEnabled())
                CreativePlugin.addToList(list, type.getStack());
        }
    }

    @Override
    public float getExplosionResistance(World world, BlockPos pos, @Nullable Entity exploder, Explosion explosion) {
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
    public boolean isBeaconBase(IBlockAccess world, BlockPos pos, BlockPos beacon) {
        return getVariant(world, pos).getBlockDef().isBeaconBase(world, pos, beacon);
    }
}
