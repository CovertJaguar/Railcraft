/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2019
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/

package mods.railcraft.common.blocks.multi;

import mods.railcraft.api.charge.Charge;
import mods.railcraft.api.charge.IChargeBlock;
import mods.railcraft.common.blocks.BlockEntityDelegate;
import mods.railcraft.common.plugins.forge.WorldPlugin;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;

/**
 * Created by CovertJaguar on 11/1/2018 for Railcraft.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public abstract class BlockMultiBlockCharge<T extends TileMultiBlockCharge> extends BlockEntityDelegate<T> implements IChargeBlock {
    protected BlockMultiBlockCharge(Material materialIn) {
        super(materialIn);
    }

    @Override
    public Charge.IAccess getMeterAccess(Charge network, IBlockState state, World world, BlockPos pos) {
        Optional<TileMultiBlock> tile = WorldPlugin.getTileEntity(world, pos, TileMultiBlock.class);
        return network.network(world).access(tile.map(TileMultiBlock::getMasterPos).orElse(pos));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
    }
}
