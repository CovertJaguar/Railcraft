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
import mods.railcraft.api.crafting.Crafters;
import mods.railcraft.common.blocks.RailcraftBlocks;
import mods.railcraft.common.plugins.color.EnumColor;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class BlockPostMetal extends BlockPostMetalBase {

    public BlockPostMetal() {
        setDefaultState(blockState.getBaseState()
                .withProperty(COLUMN, Column.FULL)
                .withProperty(COLOR, EnumColor.BLACK)
                .withProperty(NORTH, IPostConnection.ConnectStyle.NONE)
                .withProperty(SOUTH, IPostConnection.ConnectStyle.NONE)
                .withProperty(EAST, IPostConnection.ConnectStyle.NONE)
                .withProperty(WEST, IPostConnection.ConnectStyle.NONE));
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        state = state.withProperty(COLUMN, getColumnStyle(worldIn, state, pos));
        return super.getActualState(state, worldIn, pos);
    }

    @Override

    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, COLOR, COLUMN, NORTH, SOUTH, EAST, WEST);
    }

    @Override
    public void defineRecipes() {
        super.defineRecipes();
        Crafters.rollingMachine().newRecipe(RailcraftBlocks.POST_METAL.getStack(16, EnumColor.BLACK)).shaped(
                "III",
                " I ",
                "III",
                'I', "ingotIron");
        Crafters.rollingMachine().newRecipe(RailcraftBlocks.POST_METAL.getStack(16, EnumColor.BLACK)).shaped(
                "I I",
                "III",
                "I I",
                'I', "ingotIron");
    }

    @Override
    protected EnumPost getUnpaintedType() {
        return EnumPost.METAL_UNPAINTED;
    }
}
