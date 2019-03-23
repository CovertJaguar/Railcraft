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
import mods.railcraft.api.core.IPostConnection.ConnectStyle;
import mods.railcraft.common.plugins.forge.WorldPlugin;
import net.minecraft.block.Block;
import net.minecraft.block.BlockWallSign;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public final class PostConnectionHelper {

    public static ConnectStyle connectMetalPost(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side) {
        Block block = state.getBlock();
        if (block instanceof IPostConnection && ((IPostConnection) block).connectsToPost(world, pos, state, side) == ConnectStyle.NONE)
            return ConnectStyle.NONE;

        BlockPos otherPos = pos.offset(side);

        if (world.isAirBlock(otherPos))
            return ConnectStyle.NONE;

        IBlockState otherState = WorldPlugin.getBlockState(world, otherPos);
        Block otherBlock = otherState.getBlock();

        EnumFacing oppositeSide = side.getOpposite();

        if (otherBlock instanceof IPostConnection)
            return ((IPostConnection) otherBlock).connectsToPost(world, otherPos, otherState, oppositeSide);

        if (otherBlock instanceof BlockPostBase)
            return ConnectStyle.TWO_THIN;

        BlockFaceShape faceShape = otherState.getBlockFaceShape(world, otherPos, oppositeSide);

        switch (faceShape) {
            case SOLID: // general
                if (!otherState.getMaterial().isToolNotRequired() || otherBlock.getHarvestTool(otherState) != null) {
                    return otherState.isNormalCube() ? ConnectStyle.TWO_THIN : ConnectStyle.SINGLE_THICK;
                }
                return ConnectStyle.NONE;
            case MIDDLE_POLE: // side of fence post
                return ConnectStyle.TWO_THIN;
            case CENTER: // top of fence post
                return ConnectStyle.SINGLE_THICK;
            case BOWL: // top of cauldron, hopper
            case CENTER_BIG: // top of stone wall
            case CENTER_SMALL: // top of glass pane
            case MIDDLE_POLE_THIN: // side of glass pane
            case MIDDLE_POLE_THICK: // side of stone wall
                return ConnectStyle.NONE;
            default:
                // Fall through
        }

        if (otherBlock instanceof BlockWallSign && otherState.getValue(BlockWallSign.FACING) == oppositeSide) {
            return ConnectStyle.SINGLE_THICK; // Wall signs do not have defined face shapes!
        }

        return ConnectStyle.NONE;
    }

    private PostConnectionHelper() {}

}
