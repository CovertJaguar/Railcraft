/* 
 * Copyright (c) CovertJaguar, 2014 http://railcraft.info
 * 
 * This code is the property of CovertJaguar
 * and may only be used with explicit written
 * permission unless otherwise specified on the
 * license page at http://railcraft.info/wiki/info:license.
 */
package mods.railcraft.common.blocks.detector;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import mods.railcraft.common.plugins.forge.WorldPlugin;

public class ItemDetector extends ItemBlock {

    private final BlockDetector blockDetector;

    public ItemDetector(Block block) {
        super(block);
        blockDetector = (BlockDetector) block;
        setMaxDamage(0);
        setHasSubtypes(true);
        setUnlocalizedName("railcraft.detector");
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return EnumDetector.fromOrdinal(stack.getItemDamage()).getTag();
    }

    /**
     * Called to actually place the block, after the location is determined and
     * all permission checks have been made.
     *
     * @param stack    The item stack that was used to place the block. This can be
     *                 changed inside the method.
     * @param player   The player who is placing the block. Can be null if the
     *                 block is not being placed by a player.
     * @param world
     * @param pos
     * @param side     The side the player (or machine) right-clicked on.
     * @param hitX
     * @param hitY
     * @param hitZ
     * @param newState
     * @return
     */
    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
        if (!world.setBlockState(pos, newState, 3))
            return false;

        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileDetector)
            ((TileDetector) tile).setDetector(EnumDetector.fromOrdinal(stack.getItemDamage()));

        if (world.getBlockState(pos).getBlock() == blockDetector) {
            blockDetector.onBlockPlacedBy(world, pos, WorldPlugin.getBlockState(world, pos), player, stack);
        }

        return true;
    }
}
