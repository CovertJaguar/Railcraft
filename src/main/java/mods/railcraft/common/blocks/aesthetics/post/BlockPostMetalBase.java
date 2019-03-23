/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2019
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.blocks.aesthetics.post;

import mods.railcraft.api.core.IPostConnection;
import mods.railcraft.api.core.IVariantEnum;
import mods.railcraft.common.blocks.RailcraftBlocks;
import mods.railcraft.common.blocks.interfaces.IBlockColored;
import mods.railcraft.common.plugins.color.ColorPlugin;
import mods.railcraft.common.plugins.color.EnumColor;
import mods.railcraft.common.plugins.forestry.ForestryPlugin;
import mods.railcraft.common.plugins.forge.*;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;

/**
 * @author CovertJaguar <http://www.railcraft.info>
 */
public abstract class BlockPostMetalBase extends BlockPostBase implements ColorPlugin.IColorHandlerBlock, IBlockColored {

    public static final PropertyEnum<EnumColor> COLOR = PropertyEnum.create("color", EnumColor.class);

    protected BlockPostMetalBase() {
        setSoundType(SoundType.METAL);
    }

    @Override
    public void initializeDefinition() {
        HarvestPlugin.setBlockHarvestLevel("pickaxe", 2, this);

        ForestryPlugin.addBackpackItem("forestry.builder", this);

        for (EnumColor color : EnumColor.VALUES) {
            ItemStack stack = getStack(1, color);
            if (!stack.isEmpty())
                RailcraftRegistry.register(this, color, stack);
        }
    }

    @Override
    public void defineRecipes() {
        ItemStack stackColored = getWildcard();
        ItemStack stackRaw = EnumPost.METAL_UNPAINTED.getStack();

        for (EnumColor color : EnumColor.values()) {
            ItemStack outputStack = getStack(8, color);
            CraftingPlugin.addShapedRecipe(outputStack,
                    "III",
                    "IDI",
                    "III",
                    'I', stackRaw,
                    'D', color.getDyeOreDictTag());
            CraftingPlugin.addShapedRecipe(outputStack,
                    "III",
                    "IDI",
                    "III",
                    'I', stackColored,
                    'D', color.getDyeOreDictTag());
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public @Nullable StateMapperBase getStateMapper() {
        return new StateMap.Builder().ignore(COLOR).build();
    }

    @Override
    public @Nullable Class<? extends IVariantEnum> getVariantEnum() {
        return EnumColor.class;
    }

    @Override
    public IBlockState getState(@Nullable IVariantEnum variant) {
        IBlockState state = getDefaultState();
        if (variant != null) {
            checkVariant(variant);
            state = state.withProperty(COLOR, (EnumColor) variant);
        }
        return state;
    }

    @SuppressWarnings("deprecation")
    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        state = state.withProperty(NORTH, PostConnectionHelper.connectMetalPost(worldIn, pos, state, EnumFacing.NORTH));
        state = state.withProperty(SOUTH, PostConnectionHelper.connectMetalPost(worldIn, pos, state, EnumFacing.SOUTH));
        state = state.withProperty(EAST, PostConnectionHelper.connectMetalPost(worldIn, pos, state, EnumFacing.EAST));
        state = state.withProperty(WEST, PostConnectionHelper.connectMetalPost(worldIn, pos, state, EnumFacing.WEST));
        return super.getActualState(state, worldIn, pos);
    }

    @Override
    public IBlockState getItemRenderState(@Nullable IVariantEnum variant) {
        IBlockState state = getDefaultState();
        state = state.withProperty(EAST, IPostConnection.ConnectStyle.TWO_THIN);
        state = state.withProperty(WEST, IPostConnection.ConnectStyle.TWO_THIN);
        return state;
    }

    @Override
    public EnumColor getColor(IBlockState state) {
        return state.getValue(COLOR);
    }

    @Override
    public ColorPlugin.IColorFunctionBlock colorHandler() {
        return (state, worldIn, pos, tintIndex) -> getColor(state).getHexColor();
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
        for (EnumColor color : EnumColor.VALUES) {
            CreativePlugin.addToList(list, getStack(1, color));
        }
    }

    @Override
    public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        return true;
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        if (RailcraftBlocks.POST.isLoaded()) {
            drops.add(RailcraftBlocks.POST.getStack(getUnpaintedType()));
            return;
        }
        drops.add(getStack(1, EnumColor.BLACK));
    }

    protected abstract EnumPost getUnpaintedType();

    @Override
    public boolean recolorBlock(World world, BlockPos pos, EnumFacing side, EnumDyeColor color) {
        IBlockState state = WorldPlugin.getBlockState(world, pos);
        if (getColor(state).getDye() != color) {
            world.setBlockState(pos, getDefaultState().withProperty(COLOR, EnumColor.fromDye(color)));
            return true;
        }
        return false;
    }

    /**
     * Get the MapColor for this Block and the given BlockState
     */
    @SuppressWarnings("deprecation")
    @Override
    public MapColor getMapColor(IBlockState state, IBlockAccess world, BlockPos pos) {
        return getColor(state).getMapColor();
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    @SuppressWarnings("deprecation")
    @Override

    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(COLOR, EnumColor.fromOrdinal(meta));
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    @Override
    public int getMetaFromState(IBlockState state) {
        return getColor(state).ordinal();
    }

    @Override
    public String getTranslationKey() {
        return LocalizationPlugin.convertTag(super.getTranslationKey());
    }
}
