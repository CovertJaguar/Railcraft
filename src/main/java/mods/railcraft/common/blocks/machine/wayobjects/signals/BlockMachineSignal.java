/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2019
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/

package mods.railcraft.common.blocks.machine.wayobjects.signals;

import mods.railcraft.api.core.IPostConnection;
import mods.railcraft.api.core.RailcraftConstantsAPI;
import mods.railcraft.client.util.textures.TextureAtlasSheet;
import mods.railcraft.common.blocks.aesthetics.post.BlockPostBase;
import mods.railcraft.common.blocks.machine.BlockMachine;
import mods.railcraft.common.blocks.machine.IEnumMachine;
import mods.railcraft.common.plugins.forge.WorldPlugin;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Created by CovertJaguar on 9/8/2016 for Railcraft.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public abstract class BlockMachineSignal<V extends Enum<V> & IEnumMachine<V>> extends BlockMachine<V> {
    public static final PropertyEnum<EnumFacing> FRONT = PropertyEnum.create("front", EnumFacing.class, EnumFacing.HORIZONTALS);
    public static final PropertyBool CONNECTION_NORTH = PropertyBool.create("connection_north");
    public static final PropertyBool CONNECTION_SOUTH = PropertyBool.create("connection_south");
    public static final PropertyBool CONNECTION_EAST = PropertyBool.create("connection_east");
    public static final PropertyBool CONNECTION_WEST = PropertyBool.create("connection_west");
    public static final PropertyBool CONNECTION_DOWN = PropertyBool.create("connection_down");
    public static final float BLOCK_BOUNDS = 0.15f;
    public static ResourceLocation[] upperLampTextures = new ResourceLocation[4];
    public static ResourceLocation[] lowerLampTextures = new ResourceLocation[4];

    protected BlockMachineSignal() {
        super(Material.CIRCUITS);
        IBlockState defaultState = getDefaultState()
                .withProperty(FRONT, EnumFacing.NORTH)
                .withProperty(CONNECTION_NORTH, false)
                .withProperty(CONNECTION_SOUTH, false)
                .withProperty(CONNECTION_EAST, false)
                .withProperty(CONNECTION_WEST, false);

        if (defaultState.getProperties().containsKey(CONNECTION_DOWN))
            defaultState = defaultState.withProperty(CONNECTION_DOWN, true);

        setDefaultState(defaultState);
        setCreativeTab(CreativeTabs.TRANSPORTATION);
        setHarvestLevel("crowbar", 0);
        setSoundType(SoundType.METAL);
        setResistance(50);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerTextures(TextureMap textureMap) {
        super.registerTextures(textureMap);
        upperLampTextures = TextureAtlasSheet.unstitchIcons(textureMap, new ResourceLocation(RailcraftConstantsAPI.MOD_ID, "signal_lamp_top"), new Tuple<>(4, 1));
        lowerLampTextures = TextureAtlasSheet.unstitchIcons(textureMap, new ResourceLocation(RailcraftConstantsAPI.MOD_ID, "signal_lamp_bottom"), new Tuple<>(4, 1));
    }

    @SuppressWarnings("deprecation")
    @Override
    public final boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public float getBlockHardness(IBlockState state, World worldIn, BlockPos pos) {
        return 8;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public @Nullable StateMapperBase getStateMapper() {
        return new StateMap.Builder().ignore(getVariantProperty()).build();
    }

    @Override
    protected abstract BlockStateContainer createBlockState();

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        state = super.getActualState(state, worldIn, pos);
        Optional<TileSignalBase> tile = WorldPlugin.getTileEntity(worldIn, pos, TileSignalBase.class);
        tile.ifPresent(t -> t.getTileCache().resetTimers());
        EnumFacing front = tile.map(TileSignalBase::getFacing).orElse(EnumFacing.NORTH);
        state = state.withProperty(FRONT, front);
        if (state.getProperties().containsKey(CONNECTION_DOWN))
            state = state.withProperty(CONNECTION_DOWN, canConnectDown(worldIn, pos));
        state = state.withProperty(CONNECTION_NORTH, canConnectSide(worldIn, pos, EnumFacing.NORTH, front));
        state = state.withProperty(CONNECTION_EAST, canConnectSide(worldIn, pos, EnumFacing.EAST, front));
        state = state.withProperty(CONNECTION_SOUTH, canConnectSide(worldIn, pos, EnumFacing.SOUTH, front));
        state = state.withProperty(CONNECTION_WEST, canConnectSide(worldIn, pos, EnumFacing.WEST, front));
        return state;
    }

    private boolean canConnectDown(IBlockAccess worldIn, BlockPos pos) {
        BlockPos posDown = pos.down();
        IBlockState belowState = worldIn.getBlockState(posDown);
        return belowState.getBlock().canPlaceTorchOnTop(belowState, worldIn, pos);
    }

    private boolean canConnectSide(IBlockAccess world, BlockPos fromPos, EnumFacing fromSide, EnumFacing front) {
        if (fromSide == front)
            return false;
        IBlockState state = WorldPlugin.getBlockState(world, fromPos.offset(fromSide));
        Block block = state.getBlock();
        return block instanceof BlockPostBase;
    }

    @Override
    public ConnectStyle connectsToPost(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing face) {
        EnumFacing front = WorldPlugin.getTileEntity(world, pos, TileSignalBase.class).map(TileSignalBase::getFacing).orElse(EnumFacing.NORTH);
        return face != front ? ConnectStyle.SINGLE_THICK : ConnectStyle.NONE;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return face == EnumFacing.UP ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return getBoundingBox(state, world, pos);
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World world, BlockPos pos) {
        return getBoundingBox(state, world, pos).offset(pos);
    }
}
