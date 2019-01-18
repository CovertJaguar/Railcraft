/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2019
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.blocks.aesthetics.materials;

import mods.railcraft.api.core.IVariantEnum;
import mods.railcraft.api.crafting.Crafters;
import mods.railcraft.common.plugins.forestry.ForestryPlugin;
import mods.railcraft.common.plugins.forge.CraftingPlugin;
import mods.railcraft.common.plugins.forge.CreativePlugin;
import mods.railcraft.common.plugins.forge.RailcraftRegistry;
import mods.railcraft.common.util.inventory.InvTools;
import mods.railcraft.common.util.misc.Game;
import mods.railcraft.common.util.sounds.RailcraftSoundTypes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public class BlockRailcraftStairs extends BlockStairs implements IMaterialBlock {
    public static int currentRenderPass;
    static BlockRailcraftStairs block;

    public BlockRailcraftStairs() {
        super(Blocks.STONEBRICK.getDefaultState());
        setSoundType(RailcraftSoundTypes.OVERRIDE);
        setCreativeTab(CreativePlugin.STRUCTURE_TAB);
        useNeighborBrightness = true;
        hasTileEntity = true;
    }

    @Override
    public Block getObject() {
        return this;
    }

    @Override
    public @Nullable Class<? extends IVariantEnum> getVariantEnum() {
        return Materials.class;
    }

    @Override
    public String getTranslationKey(Materials mat) {
        return "tile.railcraft.stair." + mat.getLocalizationSuffix();
    }

    @Override
    public void finalizeDefinition() {
        IMaterialBlock.super.finalizeDefinition();
        for (Materials mat : Materials.getValidMats()) {
            RailcraftRegistry.register(this, mat, getStack(mat));

            switch (mat) {
                case SNOW:
                case ICE:
                    break;
                default:
                    ForestryPlugin.addBackpackItem("forestry.builder", getStack(mat));
            }

            CraftingPlugin.addShapedRecipe(getStack(4, mat), "S  ", "SS ", "SSS", 'S', mat.getSourceItem());
            Crafters.rockCrusher().makeRecipe(mat.getCraftingEquivalent())
                    .name("railcraft:stair")
                    .addOutput(mat.getSourceItem())
                    .register();
        }

        MatTools.defineCrusherRecipes(this);
    }

    @Override
    public ItemStack getStack(int qty, @Nullable IVariantEnum variant) {
        return Materials.getStack(this, qty, variant);
    }

    @Override

    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, new IProperty[]{FACING, HALF, SHAPE}, new IUnlistedProperty[]{Materials.MATERIAL_PROPERTY});
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        IExtendedBlockState actState = (IExtendedBlockState) super.getActualState(state, worldIn, pos);
        return actState.withProperty(Materials.MATERIAL_PROPERTY, MatTools.getMat(worldIn, pos));
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return MatTools.getPickBlock(state, target, world, pos, player);
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
        list.addAll(Materials.getCreativeList().stream().map(this::getStack).filter(InvTools::nonEmpty).collect(Collectors.toList()));
    }

    @SuppressWarnings("deprecation")
    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        return MatTools.getDrops(world, pos, state, fortune);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        MatTools.onBlockPlacedBy(worldIn, pos, state, placer, stack);
    }

    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, @Nullable ItemStack stack) {
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        //noinspection ConstantConditions
        player.addStat(StatList.getBlockStats(this));
        player.addExhaustion(0.005F);
        if (Game.isHost(world) && !player.capabilities.isCreativeMode) {
            dropBlockAsItem(world, pos, state, 0);
        }
        return world.setBlockToAir(pos);
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        super.breakBlock(worldIn, pos, state);
        worldIn.removeTileEntity(pos);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileMaterial();
    }

    @SuppressWarnings("deprecation")
    @Override
    public float getBlockHardness(IBlockState state, World worldIn, BlockPos pos) {
        return MatTools.getBlockHardness(state, worldIn, pos);
    }

    @Override
    public float getExplosionResistance(World world, BlockPos pos, @Nullable Entity exploder, Explosion explosion) {
        return MatTools.getExplosionResistance(world, pos, exploder, explosion);
    }

    @Override
    public SoundType getSoundType(IBlockState state, World world, BlockPos pos, @Nullable Entity entity) {
        return MatTools.getSound(world, pos);
    }
    //TODO: fix particles
//    @SideOnly(Side.CLIENT)
//    @Override
//    public boolean addHitEffects(IBlockState state, World worldObj, RayTraceResult target, ParticleManager particleManager) {
//        return ParticleHelper.addHitEffects(worldObj, block, target, particleManager, null);
//    }
//
//    @Override
//    public boolean addDestroyEffects(World world, BlockPos pos, ParticleManager particleManager) {
//        IBlockState state = WorldPlugin.getBlockState(world, pos);
//        return ParticleHelper.addDestroyEffects(world, block, pos, state, particleManager, null);
//    }

    //TODO: fix?
//    @Nonnull
//    @Override
//    public String getHarvestTool(@Nonnull IBlockState state) {
//        IBlockState matState = state.getValue(Materials.MATERIAL_PROPERTY).getState();
//        if (matState != null)
//            return matState.getBlock().getHarvestTool(matState);
//        return "pickaxe";
//    }
}
