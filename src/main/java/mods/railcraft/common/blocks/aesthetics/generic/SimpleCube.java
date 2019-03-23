/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2019
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.blocks.aesthetics.generic;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public class SimpleCube {

    public boolean canCreatureSpawn(EntityLiving.SpawnPlacementType type, IBlockAccess world, BlockPos pos) {
        return true;
    }

    public void updateTick(World world, BlockPos pos, Random rand) {
    }

    public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock) {
    }

    public void randomDisplayTick(World world, BlockPos pos, Random rand) {
    }

    public void onBlockAdded(World world, BlockPos pos) {
    }

    public boolean removedByPlayer(World world, EntityPlayer player, BlockPos pos) {
        return world.setBlockToAir(pos);
    }

    public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return 0;
    }

    public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return 0;
    }

    public boolean isFlammable(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return false;
    }

    public boolean isBeaconBase(IBlockAccess worldObj, BlockPos pos, BlockPos beacon) {
        return false;
    }

}
