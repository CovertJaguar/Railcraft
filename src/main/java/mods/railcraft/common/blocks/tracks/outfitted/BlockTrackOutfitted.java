/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2019
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.blocks.tracks.outfitted;

import mods.railcraft.api.charge.Charge;
import mods.railcraft.api.charge.IChargeBlock;
import mods.railcraft.api.core.IPostConnection;
import mods.railcraft.api.core.IVariantEnum;
import mods.railcraft.api.tracks.*;
import mods.railcraft.common.blocks.BlockMeta;
import mods.railcraft.common.blocks.RailcraftBlocks;
import mods.railcraft.common.blocks.UnlistedProperty;
import mods.railcraft.common.blocks.tracks.BlockTrackTile;
import mods.railcraft.common.blocks.tracks.TrackShapeHelper;
import mods.railcraft.common.blocks.tracks.TrackTools;
import mods.railcraft.common.blocks.tracks.behaivor.TrackTypes;
import mods.railcraft.common.core.Railcraft;
import mods.railcraft.common.plugins.forge.*;
import mods.railcraft.common.util.inventory.InvTools;
import mods.railcraft.common.util.misc.Game;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.common.property.Properties;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@BlockMeta.Tile(TileTrackOutfitted.class)
public class BlockTrackOutfitted extends BlockTrackTile<TileTrackOutfitted> implements IPostConnection, IChargeBlock, IBlockTrackOutfitted {
    // TODO: Move to rail network
    private static final Map<Charge, ChargeSpec> CHARGE_SPECS = ChargeSpec.make(Charge.distribution, ConnectType.TRACK, 0.01);
    public static final PropertyEnum<EnumRailDirection> SHAPE = PropertyEnum.create("shape", BlockRailBase.EnumRailDirection.class, TrackShapeHelper::isStraight);
    public static final PropertyBool TICKING = PropertyBool.create("ticking");
    public static final IUnlistedProperty<TrackType> TRACK_TYPE = UnlistedProperty.create("track_type", TrackType.class);
    public static final IUnlistedProperty<TrackKit> TRACK_KIT = UnlistedProperty.create("track_kit", TrackKit.class);
    public static final IUnlistedProperty<Integer> STATE = Properties.toUnlisted(PropertyInteger.create("state", 0, 15));

    public BlockTrackOutfitted() {
        setCreativeTab(CreativePlugin.TRACK_TAB);
        setHarvestLevel("crowbar", 0);
        setDefaultState(getDefaultState().withProperty(TICKING, false));
        setTickRandomly(true);
    }

    @Override
    public void defineRecipes() {
        super.defineRecipes();
        for (Tuple<TrackType, TrackKit> combo : TrackRegistry.getCombinations()) {
            TrackType trackType = combo.getFirst();
            TrackKit trackKit = combo.getSecond();
            CraftingPlugin.addShapelessRecipe(trackKit.getOutfittedTrack(trackType), trackKit.getTrackKitItem(), trackType.getBaseBlock());
        }
    }

    @Override
    public boolean place(World world, BlockPos pos, EntityLivingBase placer, EnumRailDirection shape, TrackType trackType, TrackKit trackKit) {
        return placeTrack(world, pos, placer, shape, trackType, trackKit);
    }

    public static boolean placeTrack(World world, BlockPos pos, EntityLivingBase placer, EnumRailDirection shape, TrackType trackType, TrackKit trackKit) {
        if (trackKit == TrackRegistry.getMissingTrackKit() || !trackKit.isAllowedTrackType(trackType))
            return false;
        Block block = RailcraftBlocks.TRACK_OUTFITTED.block();
        if (block != null) {
            IBlockState state = TrackToolsAPI.makeTrackState((BlockTrackOutfitted) block, shape);
            state = state.withProperty(TICKING, trackKit.requiresTicks());
            boolean placed = WorldPlugin.setBlockState(world, pos, state);
            if (placed) {
                block.onBlockPlacedBy(world, pos, state, placer, trackKit.getOutfittedTrack(trackType));
                return true;
            }
        }
        return false;
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        state = super.getExtendedState(state, world, pos);
        state = ((IExtendedBlockState) state).withProperty(TRACK_TYPE, getTrackType(world, pos));
        state = ((IExtendedBlockState) state).withProperty(TRACK_KIT, getTrackKit(world, pos));
        Optional<TileTrackOutfitted> tile = WorldPlugin.getTileEntity(world, pos, TileTrackOutfitted.class);
        state = ((IExtendedBlockState) state).withProperty(STATE, tile.map(TileTrackOutfitted::getTrackKitInstance).map(ITrackKitInstance::getRenderState).orElse(0));
        return state;
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(SHAPE, BlockRailBase.EnumRailDirection.byMetadata(meta & 7)).withProperty(TICKING, (meta & 8) > 0);
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    @Override
    public int getMetaFromState(IBlockState state) {
        int i = 0;
        i = i | state.getValue(getShapeProperty()).getMetadata();
        if (state.getValue(TICKING)) {
            i |= 8;
        }
        return i;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, new IProperty[]{getShapeProperty(), TICKING}, new IUnlistedProperty[]{TRACK_TYPE, TRACK_KIT, STATE});
    }

    @Override
    public IProperty<EnumRailDirection> getShapeProperty() {
        return SHAPE;
    }

    @Override
    public TileTrackOutfitted createTileEntity(World world, IBlockState state) {
        if (state.getValue(TICKING))
            return new TileTrackOutfittedTicking();
        return new TileTrackOutfitted();
    }

    @Override
    public void initializeDefinition() {
        RailcraftRegistry.register(TileTrackOutfitted.class, "track.outfitted");
        RailcraftRegistry.register(TileTrackOutfittedTicking.class, "track.outfitted.ticking");
        TrackToolsAPI.blockTrackOutfitted = this;
        TrackKit.blockTrackOutfitted = this;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public @Nullable StateMapperBase getStateMapper() {
        return new StateMap.Builder().ignore(getShapeProperty()).ignore(TICKING).build();
    }

    @Override
    public void registerItemModel(ItemStack stack, @Nullable IVariantEnum variant) {
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
        for (Tuple<TrackType, TrackKit> combination : TrackRegistry.getCombinations()) {
            CreativePlugin.addToList(list, combination.getSecond().getOutfittedTrack(combination.getFirst()));
        }
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        TileEntity tile = WorldPlugin.getBlockTile(world, pos);
        try {
            if (tile instanceof TileTrackOutfitted) {
                TileTrackOutfitted trackTile = (TileTrackOutfitted) tile;
                ITrackKitInstance track = trackTile.getTrackKitInstance();
                ItemStack itemStack = track.getTrackKit().getTrackKitItem();
                if (!InvTools.isEmpty(itemStack))
                    return itemStack;
            }
        } catch (Error error) {
            Game.log().api(Railcraft.MOD_ID, error, TrackRegistry.class, TrackKit.class);
        }
        return new ItemStack(this);
    }

    @Override
    public TrackType getTrackType(IBlockAccess world, BlockPos pos) {
        TileEntity tile = WorldPlugin.getBlockTile(world, pos);
        if (tile instanceof TileTrackOutfitted) {
            return ((TileTrackOutfitted) tile).getTrackType();
        }
        return TrackTypes.IRON.getTrackType();
    }

    @Override
    public TrackKit getTrackKit(IBlockAccess world, BlockPos pos) {
        TileEntity tile = WorldPlugin.getBlockTile(world, pos);
        try {
            if (tile instanceof TileTrackOutfitted) {
                return ((TileTrackOutfitted) tile).getTrackKitInstance().getTrackKit();
            }
        } catch (Error error) {
            Game.log().api(Railcraft.MOD_ID, error, ITrackKitInstance.class);
        }
        return TrackRegistry.getMissingTrackKit();
    }

    @Override
    @SuppressWarnings("deprecation")
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity tile = WorldPlugin.getBlockTile(world, pos);
        try {
            if (tile instanceof TileTrackOutfitted) {
                ITrackKitInstance track = ((TileTrackOutfitted) tile).getTrackKitInstance();
                if (track instanceof ITrackKitCustomShape)
                    return ((ITrackKitCustomShape) track).getCollisionBoundingBox(state);
            }
        } catch (Error error) {
            Game.log().api(Railcraft.MOD_ID, error, ITrackKitInstance.class);
        }
        return null;
    }

    @Override
    @SuppressWarnings("deprecation")
    public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World world, BlockPos pos) {
        TileEntity tile = WorldPlugin.getBlockTile(world, pos);
        try {
            if (tile instanceof TileTrackOutfitted) {
                ITrackKitInstance track = ((TileTrackOutfitted) tile).getTrackKitInstance();
                if (track instanceof ITrackKitCustomShape)
                    return ((ITrackKitCustomShape) track).getSelectedBoundingBox();
            }
        } catch (Error error) {
            Game.log().api(Railcraft.MOD_ID, error, ITrackKitInstance.class);
        }
        return getBoundingBox(state, world, pos).offset(pos);
    }

    @Override
    @SuppressWarnings("deprecation")
    public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d startVec, Vec3d endVec) {
        TileEntity tile = WorldPlugin.getBlockTile(world, pos);
        try {
            if (tile instanceof TileTrackOutfitted) {
                ITrackKitInstance track = ((TileTrackOutfitted) tile).getTrackKitInstance();
                if (track instanceof ITrackKitCustomShape)
                    return ((ITrackKitCustomShape) track).collisionRayTrace(startVec, endVec);
            }
        } catch (Error error) {
            Game.log().api(Railcraft.MOD_ID, error, ITrackKitInstance.class);
        }
        return super.collisionRayTrace(state, world, pos, startVec, endVec);
    }

    @Override
    public boolean isPassable(IBlockAccess world, BlockPos pos) {
        TileEntity tile = WorldPlugin.getBlockTile(world, pos);
        try {
            if (tile instanceof TileTrackOutfitted) {
                ITrackKitInstance track = ((TileTrackOutfitted) tile).getTrackKitInstance();
                if (track instanceof ITrackKitMovementBlocker)
                    return !((ITrackKitMovementBlocker) track).blocksMovement();
            }
        } catch (Error error) {
            Game.log().api(Railcraft.MOD_ID, error, ITrackKitInstance.class, ITrackKitMovementBlocker.class);
        }
        return super.isPassable(world, pos);
    }

    @Override
    public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
        if (Game.isClient(world))
            return;

        TileEntity tile = WorldPlugin.getBlockTile(world, pos);
        if (tile instanceof TileTrackOutfitted) {
            ((TileTrackOutfitted) tile).getTrackType().getEventHandler().onEntityCollision(world, pos, state, entity);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean canProvidePower(IBlockState state) {
        return true;
    }

    @Override
    public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable EnumFacing side) {
        TileEntity tile = WorldPlugin.getBlockTile(world, pos);
        if (tile instanceof TileTrackOutfitted) {
            ITrackKitInstance track = ((TileTrackOutfitted) tile).getTrackKitInstance();
            return track instanceof ITrackKitEmitter;
        }
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        TileEntity tile = WorldPlugin.getBlockTile(world, pos);
        if (tile instanceof TileTrackOutfitted) {
            ITrackKitInstance track = ((TileTrackOutfitted) tile).getTrackKitInstance();
            return track instanceof ITrackKitEmitter ? ((ITrackKitEmitter) track).getPowerOutput() : PowerPlugin.NO_POWER;
        }
        return PowerPlugin.NO_POWER;
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getStrongPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        TileEntity tile = WorldPlugin.getBlockTile(world, pos);
        if (tile instanceof TileTrackOutfitted) {
            ITrackKitInstance track = ((TileTrackOutfitted) tile).getTrackKitInstance();
            return track instanceof ITrackKitEmitter ? ((ITrackKitEmitter) track).getPowerOutput() : PowerPlugin.NO_POWER;
        }
        return PowerPlugin.NO_POWER;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean hasComparatorInputOverride(IBlockState state) {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getComparatorInputOverride(IBlockState blockState, World worldIn, BlockPos pos) {
        return WorldPlugin.getTileEntity(worldIn, pos, TileTrackOutfitted.class)
                .filter(t -> t.getTrackKitInstance() instanceof ITrackKitComparator)
                .map(t -> (ITrackKitComparator) t.getTrackKitInstance())
                .map(ITrackKitComparator::getComparatorInputOverride)
                .orElse(0);
    }

    @Override
    public void onMinecartPass(World world, EntityMinecart cart, BlockPos pos) {
        TileEntity tile = WorldPlugin.getBlockTile(world, pos);
        if (tile instanceof TileTrackOutfitted) {
            TileTrackOutfitted track = (TileTrackOutfitted) tile;
            getTrackType(world, pos).getEventHandler().onMinecartPass(world, cart, pos, track.getTrackKitInstance().getTrackKit());
            track.getTrackKitInstance().onMinecartPass(cart);
        }
    }

    @Override
    public EnumRailDirection getRailDirection(IBlockAccess world, BlockPos pos, IBlockState state, @Nullable EntityMinecart cart) {
        TileEntity tile = WorldPlugin.getBlockTile(world, pos);
        if (tile instanceof TileTrackOutfitted) {
            TileTrackOutfitted track = (TileTrackOutfitted) tile;
            EnumRailDirection shape = track.getTrackType().getEventHandler().getRailDirectionOverride(world, pos, state, cart);
            if (shape != null)
                return shape;
            return track.getTrackKitInstance().getRailDirection(state, cart);
        }
        return state.getValue(getShapeProperty());
    }

    @Override
    public float getRailMaxSpeed(World world, EntityMinecart cart, BlockPos pos) {
        TileEntity tile = WorldPlugin.getBlockTile(world, pos);
        if (tile instanceof TileTrackOutfitted)
            return ((TileTrackOutfitted) tile).getTrackKitInstance().getRailMaxSpeed(world, cart, pos);
        return 0.4f;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        TileEntity tile = WorldPlugin.getBlockTile(worldIn, pos);
        return tile instanceof TileTrackOutfitted && ((TileTrackOutfitted) tile).getTrackKitInstance().blockActivated(playerIn, hand);
    }

    @Override
    public boolean canMakeSlopes(IBlockAccess world, BlockPos pos) {
        TileEntity tile = WorldPlugin.getBlockTile(world, pos);
        return !(tile instanceof TileTrackOutfitted) || ((TileTrackOutfitted) tile).getTrackKitInstance().getTrackKit().isAllowedOnSlopes();
    }

    @Override
    public void getDrops(NonNullList<ItemStack> items, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        TileEntity tile = WorldPlugin.getBlockTile(world, pos);
        try {
            if (tile instanceof TileTrackOutfitted)
                items.addAll(((TileTrackOutfitted) tile).getTrackKitInstance().getDrops(fortune));
        } catch (Error error) {
            Game.log().api(Railcraft.MOD_ID, error, ITrackKitInstance.class, TrackKitInstance.class);
        }
    }

    @Override
    public int quantityDropped(IBlockState state, int fortune, Random random) {
        return 1;
    }

    @Override
    public boolean clearBlock(IBlockState state, World world, BlockPos pos, @Nullable EntityPlayer player) {
        TrackType trackType = getTrackType(world, pos);
        IBlockState newState = TrackToolsAPI.makeTrackState(trackType.getBaseBlock(), TrackTools.getTrackDirectionRaw(state));
        Charge.distribution.network(world).removeNode(pos);
        boolean b = WorldPlugin.setBlockState(world, pos, newState);
        world.notifyNeighborsOfStateChange(pos, this, true);
        // Below is ugly workaround for fluids!
        if (Arrays.stream(EnumFacing.VALUES).map(face -> WorldPlugin.getBlock(world, pos.offset(face))).anyMatch(block -> block instanceof IFluidBlock || block instanceof BlockLiquid)) {
            newState.getBlock().dropBlockAsItem(world, pos, newState, 0);
        }
        return b;
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        return getDefaultState();
    }

    // Determine direction here
    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        TileEntity tile = WorldPlugin.getBlockTile(world, pos);
        if (tile instanceof TileTrackOutfitted) {
            TrackType trackType = TrackRegistry.TRACK_TYPE.get(stack);
            TrackKit trackKit = TrackRegistry.TRACK_KIT.get(stack);
            TrackTileFactory.initTrackTile((TileTrackOutfitted) tile, trackType, trackKit);
            ((TileTrackOutfitted) tile).onBlockPlacedBy(state, placer, stack);
            ((TileTrackOutfitted) tile).getTrackKitInstance().onBlockPlacedBy(state, placer, stack);
            WorldPlugin.markBlockForUpdate(world, pos, state);
        }
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        try {
            TileEntity tile = WorldPlugin.getBlockTile(world, pos);
            if (tile instanceof TileTrackOutfitted)
                ((TileTrackOutfitted) tile).getTrackKitInstance().onBlockRemoved();

        } catch (Error error) {
            Game.log().api(Railcraft.MOD_ID, error, ITrackKitInstance.class);
        }
        super.breakBlock(world, pos, state);
        Charge.distribution.network(world).removeNode(pos);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos) {
        try {
            if (Game.isClient(world))
                return;
            TileEntity t = WorldPlugin.getBlockTile(world, pos);
            if (t instanceof TileTrackOutfitted) {
                TileTrackOutfitted tile = (TileTrackOutfitted) t;
                tile.onNeighborBlockChange(state, neighborBlock, neighborPos);
                tile.getTrackKitInstance().onNeighborBlockChange(state, neighborBlock);
            }
            super.neighborChanged(state, world, pos, neighborBlock, neighborPos);
        } catch (StackOverflowError error) {
            Game.log().throwable(Level.ERROR, 10, error, "Stack Overflow Error in BlockTrack.onNeighborBlockChange()");
            if (Game.DEVELOPMENT_VERSION)
                throw error;
        }
    }

    @Override
    public int getMaxSupportedDistance(World worldIn, BlockPos pos) {
        return Math.max(super.getMaxSupportedDistance(worldIn, pos), getTrackKit(worldIn, pos).getMaxSupportDistance());
    }

    @SuppressWarnings("deprecation")
    @Override
    public float getBlockHardness(IBlockState state, World world, BlockPos pos) {
        TileEntity tile = WorldPlugin.getBlockTile(world, pos);
        if (tile instanceof TileTrackOutfitted && ((TileTrackOutfitted) tile).getTrackKitInstance().isProtected())
            return -1;
        return super.getBlockHardness(state, world, pos);
    }

    @Override
    public float getExplosionResistance(World world, BlockPos pos, @Nullable Entity exploder, Explosion explosion) {
        TileEntity tile = WorldPlugin.getBlockTile(world, pos);
        if (tile instanceof TileTrackOutfitted)
            return ((TileTrackOutfitted) tile).getTrackType().getResistance() * 3f / 5f;
        return super.getExplosionResistance(world, pos, exploder, explosion);
    }

    @Override
    public ConnectStyle connectsToPost(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side) {
        TileEntity tile = WorldPlugin.getBlockTile(world, pos);
        try {
            if (tile instanceof TileTrackOutfitted) {
                ITrackKitInstance track = ((TileTrackOutfitted) tile).getTrackKitInstance();
                if (track instanceof IPostConnection)
                    return ((IPostConnection) track).connectsToPost(world, pos, state, side);
            }
        } catch (Error error) {
            Game.log().api(Railcraft.MOD_ID, error, IPostConnection.class, ITrackKitInstance.class);
        }
        return ConnectStyle.NONE;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        if (getTrackType(worldIn, pos).isElectric())
            Charge.effects().throwSparks(stateIn, worldIn, pos, rand, 75);
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        super.updateTick(worldIn, pos, state, rand);
        if (getTrackType(worldIn, pos).isElectric())
            registerNode(state, worldIn, pos);
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        super.onBlockAdded(worldIn, pos, state);
        if (getTrackType(worldIn, pos).isElectric())
            registerNode(state, worldIn, pos);
    }

    @Override
    public Map<Charge, ChargeSpec> getChargeSpecs(IBlockState state, IBlockAccess world, BlockPos pos) {
        if (getTrackType(world, pos).isElectric())
            return CHARGE_SPECS;
        else
            return Collections.emptyMap();
    }

    @Override
    public boolean canBeConnectedTo(IBlockAccess world, BlockPos pos, EnumFacing facing) {
        TileEntity tile = WorldPlugin.getBlockTile(world, pos);
        try {
            if (tile instanceof TileTrackOutfitted) {
                ITrackKitInstance track = ((TileTrackOutfitted) tile).getTrackKitInstance();
                if (track instanceof ITrackKitConnectible)
                    return ((ITrackKitConnectible) track).canBeConnectedTo(facing);
            }
        } catch (Error error) {
            Game.log().api(Railcraft.MOD_ID, error, ITrackKitInstance.class, ITrackKitConnectible.class);
        }
        return super.canBeConnectedTo(world, pos, facing);
    }
}
