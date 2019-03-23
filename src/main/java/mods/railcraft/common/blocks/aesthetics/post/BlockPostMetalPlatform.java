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
import mods.railcraft.common.blocks.RailcraftBlocks;
import mods.railcraft.common.plugins.color.EnumColor;
import mods.railcraft.common.plugins.forge.CraftingPlugin;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;

/**
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class BlockPostMetalPlatform extends BlockPostMetalBase {

    public BlockPostMetalPlatform() {
        setDefaultState(blockState.getBaseState()
                .withProperty(COLOR, EnumColor.BLACK)
                .withProperty(NORTH, IPostConnection.ConnectStyle.NONE)
                .withProperty(SOUTH, IPostConnection.ConnectStyle.NONE)
                .withProperty(EAST, IPostConnection.ConnectStyle.NONE)
                .withProperty(WEST, IPostConnection.ConnectStyle.NONE));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, COLOR, NORTH, SOUTH, EAST, WEST);
    }

    @Override
    public void defineRecipes() {
        super.defineRecipes();
        for (EnumColor color : EnumColor.VALUES) {
            CraftingPlugin.addShapedRecipe(RailcraftBlocks.POST_METAL_PLATFORM.getStack(1, color),
                    " T ",
                    " I ",
                    'T', "plateIron",
                    'I', RailcraftBlocks.POST_METAL.getStack(color));
        }
    }

    @Override
    public boolean isPlatform(IBlockState state) {
        return true;
    }

    @Override
    protected EnumPost getUnpaintedType() {
        return EnumPost.METAL_PLATFORM_UNPAINTED;
    }
}
