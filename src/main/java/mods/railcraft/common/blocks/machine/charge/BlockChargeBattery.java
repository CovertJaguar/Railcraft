/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2017
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/

package mods.railcraft.common.blocks.machine.charge;

import mods.railcraft.common.blocks.charge.ChargeManager;
import mods.railcraft.common.blocks.machine.RailcraftBlockMetadata;
import mods.railcraft.common.items.ItemCharge;
import mods.railcraft.common.items.RailcraftItems;
import mods.railcraft.common.plugins.forestry.ForestryPlugin;
import mods.railcraft.common.plugins.forge.CraftingPlugin;
import mods.railcraft.common.util.misc.AABBFactory;
import mods.railcraft.common.util.misc.RailcraftDamageSource;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Created by CovertJaguar on 7/22/2016 for Railcraft.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
@RailcraftBlockMetadata(variant = BatteryVariant.class)
public class BlockChargeBattery extends BlockMachineCharge<BatteryVariant> {
    public static final AxisAlignedBB COLLISION_BOX = AABBFactory.start().box().raiseCeiling(-0.0625D).build();

    public BlockChargeBattery() {
        IBlockState defaultState = blockState.getBaseState().withProperty(getVariantProperty(), BatteryVariant.NICKEL_IRON);
        setDefaultState(defaultState);
        setResistance(10F);
        setHardness(5F);
    }

    @Override
    public void initializeDefinintion() {
        ForestryPlugin.addBackpackItem("forestry.builder", this);
    }

    @Override
    public void defineRecipes() {
        BatteryVariant.NICKEL_IRON.ifAvailable(v ->
                CraftingPlugin.addRecipe(getStack(v),
                        "TWT",
                        "NSI",
                        "NBI",
                        'T', RailcraftItems.CHARGE, ItemCharge.EnumCharge.TERMINAL,
                        'N', RailcraftItems.CHARGE, ItemCharge.EnumCharge.ELECTRODE_NICKEL,
                        'I', RailcraftItems.CHARGE, ItemCharge.EnumCharge.ELECTRODE_IRON,
                        'W', RailcraftItems.CHARGE, ItemCharge.EnumCharge.SPOOL_MEDIUM,
                        'S', "dustSaltpeter",
                        'B', Items.WATER_BUCKET));
    }

    /**
     * Called When an Entity Collided with the Block
     */
    @Override
    public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity) {
        super.onEntityCollidedWithBlock(world, pos, state, entity);
        ChargeManager.zapEntity(world, pos, state, entity, RailcraftDamageSource.ELECTRIC, 1F, 1000.0);
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, World worldIn, BlockPos pos) {
        return COLLISION_BOX;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileChargeBattery(getVariant(state));
    }

    @Nullable
    @Override
    public ChargeDef getChargeDef(IBlockState state, IBlockAccess world, BlockPos pos) {
        return getVariant(state).chargeDef;
    }
}
