/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2021
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/

package mods.railcraft.common.blocks.single;

import mods.railcraft.client.render.tesr.TESRPneumaticEngine;
import mods.railcraft.common.blocks.BlockContainerRailcraft;
import mods.railcraft.common.blocks.BlockEntityDelegate;
import mods.railcraft.common.blocks.BlockMeta;
import mods.railcraft.common.util.effects.HostEffects;
import mods.railcraft.common.util.misc.MathTools;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleDigging;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.EnumFacing.Plane;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static net.minecraft.util.EnumFacing.AxisDirection.POSITIVE;

public class BlockEngine extends BlockAnimated<TileEngine> {
    public static final PropertyDirection FACING = PropertyDirection.create("facing");
    private final String name;
    private final Class<? extends TileEngine> tileClass;

    public BlockEngine(String name, Class<? extends TileEngine> tileClass) {
        super(Material.IRON);
        setDefaultState(getDefaultState().withProperty(FACING, EnumFacing.UP));
        this.name = name;
        this.tileClass = tileClass;
    }

    @Override
    public Class<? extends TileEngine> getTileClass(IBlockState state) {
        return tileClass;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getIndex();
    }

    @Override
    public int damageDropped(IBlockState state) {
        return 0;
    }

    @Override
    @SuppressWarnings("deprecation")
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(FACING, EnumFacing.byIndex(meta));
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        return getDefaultState().withProperty(FACING, EnumFacing.UP);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void initializeClient() {
        super.initializeClient();
        ClientRegistry.bindTileEntitySpecialRenderer(tileClass, new TESRPneumaticEngine(name));
    }

    @Override
    public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        EnumFacing facing = state.getValue(FACING);
        return facing.getOpposite() == side;
    }
}
