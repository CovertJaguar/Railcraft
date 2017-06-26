/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2017
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/

package mods.railcraft.common.blocks.tracks.behaivor;

import mods.railcraft.common.blocks.charge.ChargeManager;
import mods.railcraft.common.util.misc.RailcraftDamageSource;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Created by CovertJaguar on 8/7/2016 for Railcraft.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public enum CollisionHandler {
    NULL,
    ELECTRIC {
        @Override
        public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity) {
            ChargeManager.zapEntity(world, pos, state, entity, RailcraftDamageSource.TRACK_ELECTRIC, 2F, 2000.0);
        }
    };

    public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity) {
    }
}
