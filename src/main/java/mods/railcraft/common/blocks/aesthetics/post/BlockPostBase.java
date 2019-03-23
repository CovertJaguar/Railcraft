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
import mods.railcraft.common.blocks.BlockRailcraft;
import mods.railcraft.common.plugins.forge.CreativePlugin;
import mods.railcraft.common.plugins.forge.WorldPlugin;
import mods.railcraft.common.util.misc.AABBFactory;
import mods.railcraft.common.util.sounds.RailcraftSoundTypes;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

import static net.minecraft.util.EnumFacing.DOWN;
import static net.minecraft.util.EnumFacing.UP;

public abstract class BlockPostBase extends BlockRailcraft {

    public static final PropertyEnum<Column> COLUMN = PropertyEnum.create("column", Column.class);
    public static final PropertyEnum<IPostConnection.ConnectStyle> NORTH = PropertyEnum.create("north", IPostConnection.ConnectStyle.class);
    public static final PropertyEnum<IPostConnection.ConnectStyle> SOUTH = PropertyEnum.create("south", IPostConnection.ConnectStyle.class);
    public static final PropertyEnum<IPostConnection.ConnectStyle> EAST = PropertyEnum.create("east", IPostConnection.ConnectStyle.class);
    public static final PropertyEnum<IPostConnection.ConnectStyle> WEST = PropertyEnum.create("west", IPostConnection.ConnectStyle.class);

    public enum Column implements IStringSerializable {
        FULL,
        MINI,
        NONE;

        @Override
        public String getName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    private static final float SIZE = 0.15f;
    private static final float SELECT = 4F / 16F;
    private static final AxisAlignedBB BOUNDING_BOX = AABBFactory.start().box().expandHorizontally(-0.2).build();
    private static final AxisAlignedBB COLLISION_BOX = AABBFactory.start().box().expandHorizontally(-SIZE).build();
    private static final AxisAlignedBB COLLISION_BOX_FENCE = AABBFactory.start().box().expandHorizontally(-SIZE).raiseCeiling(0.5).build();

    protected BlockPostBase() {
        super(Material.IRON);
        setSoundType(RailcraftSoundTypes.OVERRIDE);
        setResistance(15);
        setHardness(3);

        setCreativeTab(CreativePlugin.STRUCTURE_TAB);
    }

    public Column getColumnStyle(IBlockAccess world, IBlockState state, BlockPos pos) {
        if (world.isSideSolid(pos.down(), EnumFacing.UP, true) || PostConnectionHelper.connectMetalPost(world, pos, state, EnumFacing.DOWN) != IPostConnection.ConnectStyle.NONE)
            return Column.FULL;
        BlockPos up = pos.up();
        IBlockState above = WorldPlugin.getBlockState(world, up);
        if (above.getBlock() instanceof BlockPostBase)
            return Column.FULL;
        if (!isPlatform(state) && !WorldPlugin.isBlockAir(world, up, above))
            return Column.MINI;
        return Column.FULL;
    }

    public boolean isPlatform(IBlockState state) {
        return false;
    }

    @Override
    public int damageDropped(IBlockState state) {
        return getMetaFromState(state);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        if (isPlatform(state))
            return FULL_BLOCK_AABB;
        else
            return BOUNDING_BOX;
    }

    @Override
    public @Nullable AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        if (isPlatform(blockState))
            return FULL_BLOCK_AABB;
        BlockPos down = pos.down();
        IBlockState downState = WorldPlugin.getBlockState(worldIn, down);
        if (!downState.getBlock().isAir(downState, worldIn, down) && !(downState.getBlock() instanceof BlockPostBase))
            return COLLISION_BOX_FENCE;
        return COLLISION_BOX;
    }


    @Override
    public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World worldIn, BlockPos pos) {
        if (isPlatform(state))
            return AABBFactory.start().createBoxForTileAt(pos).build();
        return AABBFactory.start().createBoxForTileAt(pos).expandHorizontally(-SELECT).build();
    }

    @Override
    public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return false;
    }

    @Override
    public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        return side == DOWN || (isPlatform(state) && side == UP);
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        if (face.getAxis() == EnumFacing.Axis.Y) {
            return BlockFaceShape.CENTER;
        }
        return BlockFaceShape.MIDDLE_POLE;
    }

    @Override
    public boolean canPlaceTorchOnTop(IBlockState state, IBlockAccess world, BlockPos pos) {
        return true;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isBlockNormalCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean canBeReplacedByLeaves(IBlockState state, IBlockAccess world, BlockPos pos) {
        return false;
    }

    @Override
    public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    public boolean canBeConnectedTo(IBlockAccess world, BlockPos pos, EnumFacing facing) {
        BlockPos offset = pos.offset(facing);
        return PostConnectionHelper.connectMetalPost(world, offset, WorldPlugin.getBlockState(world, offset), facing.getOpposite()) != ConnectStyle.NONE;
    }
}
