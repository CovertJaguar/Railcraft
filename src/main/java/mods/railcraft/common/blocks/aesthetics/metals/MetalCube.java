/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2019
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/

package mods.railcraft.common.blocks.aesthetics.metals;

import mods.railcraft.common.blocks.aesthetics.generic.SimpleCube;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class MetalCube extends SimpleCube {

    @Override
    public boolean isBeaconBase(IBlockAccess worldObj, BlockPos pos, BlockPos beacon) {
        return true;
    }
}
