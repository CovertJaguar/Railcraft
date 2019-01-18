/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2019
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.blocks.multi;

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import mods.railcraft.common.blocks.RailcraftBlocks;
import mods.railcraft.common.blocks.machine.ITankTile;
import mods.railcraft.common.core.RailcraftConfig;
import mods.railcraft.common.fluids.FluidItemHelper;
import mods.railcraft.common.fluids.FluidTools;
import mods.railcraft.common.fluids.Fluids;
import mods.railcraft.common.fluids.TankManager;
import mods.railcraft.common.fluids.tanks.StandardTank;
import mods.railcraft.common.gui.EnumGui;
import mods.railcraft.common.plugins.forge.WorldPlugin;
import mods.railcraft.common.util.inventory.InventoryAdvanced;
import mods.railcraft.common.util.misc.Game;
import mods.railcraft.common.util.misc.Timer;
import mods.railcraft.common.util.network.RailcraftInputStream;
import mods.railcraft.common.util.network.RailcraftOutputStream;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author CovertJaguar <http://www.railcraft.info>
 */
public abstract class TileTankBase extends TileMultiBlock implements ITankTile {

    @SuppressWarnings("WeakerAccess")
    protected static final int SLOT_INPUT = 0;
    @SuppressWarnings("WeakerAccess")
    protected static final int SLOT_OUTPUT = 1;
    private static final int NETWORK_UPDATE_INTERVAL = 64;
    private static final List<MultiBlockPattern> patterns = buildPatterns();
    protected final StandardTank tank = new StandardTank(64 * FluidTools.BUCKET_VOLUME, this);
    protected final TankManager tankManager = new TankManager();
    private final InventoryAdvanced inv;
    private final Timer networkTimer = new Timer();
    private FluidStack previousFluidStack;

    protected TileTankBase() {
        super(patterns);
        inv = new InventoryAdvanced(2, "gui.tank.iron").callbackTile(this);
        tankManager.add(tank);
    }

    public static void placeIronTank(World world, BlockPos pos, int patternIndex, FluidStack fluid) {
        MultiBlockPattern pattern = TileTankBase.patterns.get(patternIndex);
        Char2ObjectMap<IBlockState> blockMapping = new Char2ObjectOpenHashMap<>();
        blockMapping.put('B', RailcraftBlocks.TANK_IRON_WALL.getDefaultState());
        blockMapping.put('W', RailcraftBlocks.TANK_IRON_GAUGE.getDefaultState());
        TileEntity tile = pattern.placeStructure(world, pos, blockMapping);
        if (tile instanceof TileTankBase) {
            TileTankBase master = (TileTankBase) tile;
            master.tank.setFluid(fluid);
        }
    }

    public static void placeSteelTank(World world, BlockPos pos, int patternIndex, FluidStack fluid) {
        MultiBlockPattern pattern = TileTankBase.patterns.get(patternIndex);
        Char2ObjectMap<IBlockState> blockMapping = new Char2ObjectOpenHashMap<>();
        blockMapping.put('B', RailcraftBlocks.TANK_STEEL_WALL.getDefaultState());
        blockMapping.put('W', RailcraftBlocks.TANK_STEEL_GAUGE.getDefaultState());
        TileEntity tile = pattern.placeStructure(world, pos, blockMapping);
        if (tile instanceof TileTankBase) {
            TileTankBase master = (TileTankBase) tile;
            master.tank.setFluid(fluid);
        }
    }

    private static List<MultiBlockPattern> buildPatterns() {
        List<MultiBlockPattern> pats = new ArrayList<>();
        boolean client = FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT;

        // 3x3
        int xOffset = 2;
        int yOffset = 0;
        int zOffset = 2;

        char[][] bottom = {
                {'O', 'O', 'O', 'O', 'O'},
                {'O', 'B', 'B', 'B', 'O'},
                {'O', 'B', 'M', 'B', 'O'},
                {'O', 'B', 'B', 'B', 'O'},
                {'O', 'O', 'O', 'O', 'O'}
        };

        char[][] middle = {
                {'O', 'O', 'O', 'O', 'O'},
                {'O', 'B', 'W', 'B', 'O'},
                {'O', 'W', 'A', 'W', 'O'},
                {'O', 'B', 'W', 'B', 'O'},
                {'O', 'O', 'O', 'O', 'O'}
        };

        char[][] top = {
                {'O', 'O', 'O', 'O', 'O'},
                {'O', 'B', 'B', 'B', 'O'},
                {'O', 'B', 'T', 'B', 'O'},
                {'O', 'B', 'B', 'B', 'O'},
                {'O', 'O', 'O', 'O', 'O'}
        };

        char[][] border = {
                {'O', 'O', 'O', 'O', 'O'},
                {'O', 'O', 'O', 'O', 'O'},
                {'O', 'O', 'O', 'O', 'O'},
                {'O', 'O', 'O', 'O', 'O'},
                {'O', 'O', 'O', 'O', 'O'}
        };

        for (int i = 4; i <= 8; i++) {
            char[][][] map = buildMap(i, bottom, middle, top, border);
            AxisAlignedBB entityCheck = new AxisAlignedBB(0, 1, 0, 1, i - 1, 1);
            pats.add(buildPattern(map, xOffset, yOffset, zOffset, entityCheck));
        }

        // 5x5
        if (client || RailcraftConfig.getMaxTankSize() >= 5) {
            xOffset = zOffset = 3;

            bottom = new char[][]{
                    {'O', 'O', 'O', 'O', 'O', 'O', 'O'},
                    {'O', 'B', 'B', 'B', 'B', 'B', 'O'},
                    {'O', 'B', 'W', 'W', 'W', 'B', 'O'},
                    {'O', 'B', 'W', 'M', 'W', 'B', 'O'},
                    {'O', 'B', 'W', 'W', 'W', 'B', 'O'},
                    {'O', 'B', 'B', 'B', 'B', 'B', 'O'},
                    {'O', 'O', 'O', 'O', 'O', 'O', 'O'}
            };

            middle = new char[][]{
                    {'O', 'O', 'O', 'O', 'O', 'O', 'O'},
                    {'O', 'B', 'W', 'W', 'W', 'B', 'O'},
                    {'O', 'W', 'A', 'A', 'A', 'W', 'O'},
                    {'O', 'W', 'A', 'A', 'A', 'W', 'O'},
                    {'O', 'W', 'A', 'A', 'A', 'W', 'O'},
                    {'O', 'B', 'W', 'W', 'W', 'B', 'O'},
                    {'O', 'O', 'O', 'O', 'O', 'O', 'O'}
            };

            top = new char[][]{
                    {'O', 'O', 'O', 'O', 'O', 'O', 'O'},
                    {'O', 'B', 'B', 'B', 'B', 'B', 'O'},
                    {'O', 'B', 'W', 'W', 'W', 'B', 'O'},
                    {'O', 'B', 'W', 'T', 'W', 'B', 'O'},
                    {'O', 'B', 'W', 'W', 'W', 'B', 'O'},
                    {'O', 'B', 'B', 'B', 'B', 'B', 'O'},
                    {'O', 'O', 'O', 'O', 'O', 'O', 'O'}
            };

            border = new char[][]{
                    {'O', 'O', 'O', 'O', 'O', 'O', 'O'},
                    {'O', 'O', 'O', 'O', 'O', 'O', 'O'},
                    {'O', 'O', 'O', 'O', 'O', 'O', 'O'},
                    {'O', 'O', 'O', 'O', 'O', 'O', 'O'},
                    {'O', 'O', 'O', 'O', 'O', 'O', 'O'},
                    {'O', 'O', 'O', 'O', 'O', 'O', 'O'},
                    {'O', 'O', 'O', 'O', 'O', 'O', 'O'}
            };

            for (int i = 4; i <= 8; i++) {
                char[][][] map = buildMap(i, bottom, middle, top, border);
                AxisAlignedBB entityCheck = new AxisAlignedBB(-1, 1, -1, 2, i - 1, 2);
                pats.add(buildPattern(map, xOffset, yOffset, zOffset, entityCheck));
            }
        }

        // 7x7
        if (client || RailcraftConfig.getMaxTankSize() >= 7) {
            xOffset = zOffset = 4;

            bottom = new char[][]{
                    {'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O'},
                    {'O', 'B', 'B', 'B', 'B', 'B', 'B', 'B', 'O'},
                    {'O', 'B', 'W', 'W', 'W', 'W', 'W', 'B', 'O'},
                    {'O', 'B', 'W', 'W', 'W', 'W', 'W', 'B', 'O'},
                    {'O', 'B', 'W', 'W', 'M', 'W', 'W', 'B', 'O'},
                    {'O', 'B', 'W', 'W', 'W', 'W', 'W', 'B', 'O'},
                    {'O', 'B', 'W', 'W', 'W', 'W', 'W', 'B', 'O'},
                    {'O', 'B', 'B', 'B', 'B', 'B', 'B', 'B', 'O'},
                    {'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O'}
            };

            middle = new char[][]{
                    {'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O'},
                    {'O', 'B', 'W', 'W', 'W', 'W', 'W', 'B', 'O'},
                    {'O', 'W', 'A', 'A', 'A', 'A', 'A', 'W', 'O'},
                    {'O', 'W', 'A', 'A', 'A', 'A', 'A', 'W', 'O'},
                    {'O', 'W', 'A', 'A', 'A', 'A', 'A', 'W', 'O'},
                    {'O', 'W', 'A', 'A', 'A', 'A', 'A', 'W', 'O'},
                    {'O', 'W', 'A', 'A', 'A', 'A', 'A', 'W', 'O'},
                    {'O', 'B', 'W', 'W', 'W', 'W', 'W', 'B', 'O'},
                    {'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O'}
            };

            top = new char[][]{
                    {'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O'},
                    {'O', 'B', 'B', 'B', 'B', 'B', 'B', 'B', 'O'},
                    {'O', 'B', 'W', 'W', 'W', 'W', 'W', 'B', 'O'},
                    {'O', 'B', 'W', 'W', 'W', 'W', 'W', 'B', 'O'},
                    {'O', 'B', 'W', 'W', 'T', 'W', 'W', 'B', 'O'},
                    {'O', 'B', 'W', 'W', 'W', 'W', 'W', 'B', 'O'},
                    {'O', 'B', 'W', 'W', 'W', 'W', 'W', 'B', 'O'},
                    {'O', 'B', 'B', 'B', 'B', 'B', 'B', 'B', 'O'},
                    {'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O'}
            };

            border = new char[][]{
                    {'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O'},
                    {'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O'},
                    {'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O'},
                    {'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O'},
                    {'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O'},
                    {'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O'},
                    {'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O'},
                    {'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O'},
                    {'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O'}
            };

            for (int i = 4; i <= 8; i++) {
                char[][][] map = buildMap(i, bottom, middle, top, border);
                AxisAlignedBB entityCheck = new AxisAlignedBB(-2, 1, -2, 3, i - 1, 3);
                pats.add(buildPattern(map, xOffset, yOffset, zOffset, entityCheck));
            }
        }

        // 9x9
        if (client || RailcraftConfig.getMaxTankSize() >= 9) {
            xOffset = zOffset = 5;

            bottom = new char[][]{
                    {'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O'},
                    {'O', 'B', 'B', 'B', 'B', 'B', 'B', 'B', 'B', 'B', 'O'},
                    {'O', 'B', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'B', 'O'},
                    {'O', 'B', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'B', 'O'},
                    {'O', 'B', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'B', 'O'},
                    {'O', 'B', 'W', 'W', 'W', 'M', 'W', 'W', 'W', 'B', 'O'},
                    {'O', 'B', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'B', 'O'},
                    {'O', 'B', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'B', 'O'},
                    {'O', 'B', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'B', 'O'},
                    {'O', 'B', 'B', 'B', 'B', 'B', 'B', 'B', 'B', 'B', 'O'},
                    {'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O'}
            };

            middle = new char[][]{
                    {'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O'},
                    {'O', 'B', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'B', 'O'},
                    {'O', 'W', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'W', 'O'},
                    {'O', 'W', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'W', 'O'},
                    {'O', 'W', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'W', 'O'},
                    {'O', 'W', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'W', 'O'},
                    {'O', 'W', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'W', 'O'},
                    {'O', 'W', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'W', 'O'},
                    {'O', 'W', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'W', 'O'},
                    {'O', 'B', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'B', 'O'},
                    {'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O'}
            };

            top = new char[][]{
                    {'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O'},
                    {'O', 'B', 'B', 'B', 'B', 'B', 'B', 'B', 'B', 'B', 'O'},
                    {'O', 'B', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'B', 'O'},
                    {'O', 'B', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'B', 'O'},
                    {'O', 'B', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'B', 'O'},
                    {'O', 'B', 'W', 'W', 'W', 'T', 'W', 'W', 'W', 'B', 'O'},
                    {'O', 'B', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'B', 'O'},
                    {'O', 'B', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'B', 'O'},
                    {'O', 'B', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'B', 'O'},
                    {'O', 'B', 'B', 'B', 'B', 'B', 'B', 'B', 'B', 'B', 'O'},
                    {'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O'}
            };

            border = new char[][]{
                    {'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O'},
                    {'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O'},
                    {'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O'},
                    {'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O'},
                    {'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O'},
                    {'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O'},
                    {'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O'},
                    {'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O'},
                    {'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O'},
                    {'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O'},
                    {'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O'}
            };

            for (int i = 4; i <= 8; i++) {
                char[][][] map = buildMap(i, bottom, middle, top, border);
                AxisAlignedBB entityCheck = new AxisAlignedBB(-3, 1, -3, 4, i - 1, 4);
                pats.add(buildPattern(map, xOffset, yOffset, zOffset, entityCheck));
            }
        }

        return pats;
    }

    private static MultiBlockPattern buildPattern(char[][][] map, int xOffset, int yOffset, int zOffset, AxisAlignedBB entityCheck) {
        if (!RailcraftConfig.allowTankStacking()) {
            entityCheck.offset(0, 1, 0);
            yOffset = 1;
        }
        return new MultiBlockPattern(map, xOffset, yOffset, zOffset, entityCheck);
    }

    private static char[][][] buildMap(int height, char[][] bottom, char[][] mid, char[][] top, char[][] border) {
        char[][][] map;
        if (RailcraftConfig.allowTankStacking()) {
            map = new char[height][][];

            map[0] = bottom;
            map[height - 1] = top;

            for (int i = 1; i < height - 1; i++) {
                map[i] = mid;
            }
        } else {
            map = new char[height + 2][][];

            map[0] = border;
            map[1] = bottom;
            map[height] = top;
            map[height + 1] = border;

            for (int i = 2; i < height; i++) {
                map[i] = mid;
            }
        }

        return map;
    }

    @Override
    public final EnumGui getGui() {
        return EnumGui.TANK;
    }

    public TankDefinition getTankDefinition() {
        return TankDefinition.IRON;
    }

    @Override
    public IInventory getInventory() {
        return inv;
    }

    @Override
    public float getResistance(@Nullable Entity exploder) {
        return getTankDefinition().getResistance(exploder);
    }

    @Override
    protected int getMaxRecursionDepth() {
        return 500;
    }

    @Override
    public String getTitle() {
        return getTankDefinition().getTitle();
    }

    @Override
    protected boolean isStructureTile(@Nullable TileEntity tile) {
        return tile instanceof TileTankBase;
    }

    @Override
    public boolean blockActivated(EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        ItemStack heldItem = player.getHeldItem(hand);
        if (Game.isHost(world)) {
            if (isStructureValid() && FluidTools.interactWithFluidHandler(player, hand, getTankManager())) {
                TileTankBase master = (TileTankBase) getMasterBlock();
                if (master != null)
                    master.syncClient();
                return true;
            }
        } else if (FluidItemHelper.isContainer(heldItem))
            return true;

        // Prevents players from getting inside tanks using boats
        return heldItem.getItem() == Items.BOAT || super.blockActivated(player, hand, side, hitX, hitY, hitZ);
    }

    @Override
    public TankManager getTankManager() {
        TileTankBase mBlock = (TileTankBase) getMasterBlock();
        if (mBlock != null)
            return mBlock.tankManager;
        return TankManager.NIL;
    }

    @Override
    public @Nullable StandardTank getTank() {
        TileTankBase mBlock = (TileTankBase) getMasterBlock();
        if (mBlock != null)
            return mBlock.tankManager.get(0);
        return null;
    }

    @Override
    protected void onPatternLock(MultiBlockPattern pattern) {
        if (isMaster) {
            int capacity = (pattern.getPatternWidthX() - 2) * (pattern.getPatternHeight() - (pattern.getMasterOffset().getY() * 2)) * (pattern.getPatternWidthZ() - 2) * getTankDefinition().getCapacityPerBlock();
            tankManager.setCapacity(0, capacity);
        }
    }

    @Override
    protected void onMasterChanged() {
        super.onMasterChanged();
        TankManager tMan = getTankManager();
        if (!tMan.isEmpty())
            tMan.get(0).setFluid(null);
    }

    @Override
    protected boolean isMapPositionValid(BlockPos pos, char mapPos) {
        IBlockState state = WorldPlugin.getBlockState(world, pos);
        switch (mapPos) {
            case 'O': // Other
                return !getTankDefinition().isTankBlock(state);
            case 'W': // Wall, Gauge, or Valve
                return getTankDefinition().isTankBlock(state);
            case 'B': // Wall
                return getTankDefinition().isWallBlock(state);
            case 'M': // Master
            case 'T': // Top Block
                if (!getTankDefinition().isTankBlock(state))
                    return false;
                TileEntity tile = world.getTileEntity(pos);
                if (!(tile instanceof TileMultiBlock)) {
                    world.removeTileEntity(pos);
                    return true;
                }
                return !((TileMultiBlock) tile).isStructureValid();
            case 'A': // Air
                return state.getBlock().isAir(state, world, pos);
        }
        return true;
    }

    @Override
    public void update() {
        super.update();

        if (Game.isHost(world))
            if (isMaster) {

                if (clock % FluidTools.BUCKET_FILL_TIME == 0)
                    FluidTools.processContainers(tankManager.get(0), inv, SLOT_INPUT, SLOT_OUTPUT);

                if (networkTimer.hasTriggered(world, NETWORK_UPDATE_INTERVAL))
                    syncClient();
            }
    }

    @SuppressWarnings("ConstantConditions")
    private void syncClient() {
        FluidStack fluidStack = tankManager.get(0).getFluid();
        if (!Fluids.areIdentical(previousFluidStack, fluidStack)) {
            previousFluidStack = fluidStack == null ? null : fluidStack.copy();
            sendUpdateToClient();
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        tankManager.writeTanksToNBT(data);
        inv.writeToNBT("inv", data);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        tankManager.readTanksFromNBT(data);
        inv.readFromNBT("inv", data);
    }

    @Override
    public void writePacketData(RailcraftOutputStream data) throws IOException {
        super.writePacketData(data);
        tankManager.writePacketData(data);
    }

    @Override
    public void readPacketData(RailcraftInputStream data) throws IOException {
        super.readPacketData(data);
        tankManager.readPacketData(data);
    }

    @Override
    public double getMaxRenderDistanceSquared() {
        return Short.MAX_VALUE;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return isMaster ? pass == 0 : pass == 1;
    }

    int getComparatorValue() {
        double fullness = (double) tank.getFluidAmount() / (double) tank.getCapacity();
        return (int) Math.ceil(fullness * 15.0);
    }
}
