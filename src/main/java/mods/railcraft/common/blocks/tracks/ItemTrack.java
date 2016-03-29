/* 
 * Copyright (c) CovertJaguar, 2014 http://railcraft.info
 * 
 * This code is the property of CovertJaguar
 * and may only be used with explicit written
 * permission unless otherwise specified on the
 * license page at http://railcraft.info/wiki/info:license.
 */
package mods.railcraft.common.blocks.tracks;

import mods.railcraft.api.core.items.ITrackItem;
import mods.railcraft.api.tracks.ITrackCustomPlaced;
import mods.railcraft.api.tracks.ITrackInstance;
import mods.railcraft.api.tracks.TrackRegistry;
import mods.railcraft.api.tracks.TrackSpec;
import mods.railcraft.common.blocks.RailcraftBlocks;
import mods.railcraft.common.util.inventory.InvTools;
import mods.railcraft.common.util.misc.Game;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ItemTrack extends ItemBlock implements ITrackItem {

    public ItemTrack(Block block) {
        super(block);
        setMaxDamage(0);
        setHasSubtypes(true);
        setUnlocalizedName("railcraft.track");
    }

//    @Override
//    public IIcon getIcon(ItemStack stack, int pass) {
//        return getIconIndex(stack);
//    }
//
//    @Override
//    public IIcon getIconIndex(ItemStack stack) {
//        TrackSpec trackSpec = getTrackSpec(stack);
//        if (trackSpec == null)
//            return Blocks.rail.getIcon(0, 0);
//        return trackSpec.getItemIcon();
//    }

    public TrackSpec getTrackSpec(ItemStack stack) {
        if (stack != null && stack.getItem() == this) {
            NBTTagCompound nbt = InvTools.getItemData(stack);
            if (nbt.hasKey("track"))
                return TrackRegistry.getTrackSpec(nbt.getString("track"));
            return TrackRegistry.getTrackSpec(-1);
        }
        return null;
    }

//    /**
//     * Returns 0 for /terrain.png, 1 for /gui/items.png
//     */
//    @Override
//    @SideOnly(Side.CLIENT)
//    public int getSpriteNumber() {
//        return 0;
//    }
//
//    @Override
//    @SideOnly(Side.CLIENT)
//    public void registerIcons(IIconRegister par1IconRegister) {
//    }

    @Override
    public int getMetadata(int i) {
        return 0;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return "tile." + getTrackSpec(stack).getTrackTag().replace(':', '.');
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean advanced) {
        super.addInformation(stack, player, list, advanced);
        try {
            TrackSpec spec = getTrackSpec(stack);
            List<String> tips = spec.getItemToolTip();
            if (tips != null)
                list.addAll(tips);
        } catch (Throwable error) {
            Game.logErrorAPI("Railcraft", error, TrackSpec.class);
        }
    }

    @Override
    public Block getPlacedBlock() {
        return RailcraftBlocks.getBlockTrack();
    }

    @Override
    public boolean isPlacedTileEntity(ItemStack stack, TileEntity tile) {
        if (tile instanceof TileTrack) {
            TileTrack track = (TileTrack) tile;
            if (track.getTrackInstance().getTrackSpec() == getTrackSpec(stack))
                return true;
        }
        return false;
    }

    @Override
    public boolean placeTrack(ItemStack stack, World world, BlockPos pos) {
        return placeTrack(stack, world, pos, EnumFacing.UP);
    }

    private boolean placeTrack(ItemStack stack, World world, BlockPos pos, EnumFacing face) {
        Block blockTrack = RailcraftBlocks.getBlockTrack();
        if (blockTrack == null)
            return false;
        if (pos.getY() >= world.getHeight() - 1)
            return false;
        if (stack == null || !(stack.getItem() instanceof ItemTrack))
            return false;
        TrackSpec spec = getTrackSpec(stack);
        ITrackInstance track = spec.createInstanceFromSpec();
        boolean canPlace = world.canBlockBePlaced(blockTrack, pos, true, face, null, stack);
        if (track instanceof ITrackCustomPlaced)
            canPlace &= ((ITrackCustomPlaced) track).canPlaceRailAt(world, pos);
        else
            canPlace &= world.isSideSolid(pos.down(), EnumFacing.UP);
        if (canPlace) {
            boolean placed = world.setBlockState(pos, blockTrack.getDefaultState());
            // System.out.println("Block placement attempted");
            if (placed) {
                if (world.getBlockState(pos).getBlock() == blockTrack) {
                    TileTrack tile = TrackFactory.makeTrackTile(track);
                    world.setTileEntity(pos, tile);
                    blockTrack.onBlockAdded(world, pos, world.getBlockState(pos));
                    world.markBlockForUpdate(pos);
                }
                double x = pos.getX() + 0.5;
                double y = pos.getY() + 0.5;
                double z = pos.getZ() + 0.5;
                String sound = this.block.stepSound.getPlaceSound();
                world.playSoundEffect(x, y, z, sound, (this.block.stepSound.getVolume() + 1.0F) / 2.0F, this.block.stepSound.getFrequency() * 0.8F);}
            return true;
        } else
            return false;
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (stack.stackSize <= 0)
            return false;

        Block block = world.getBlockState(pos).getBlock();
        if (block == Blocks.snow_layer)
            side = EnumFacing.DOWN;
        else if (block != Blocks.vine && block != Blocks.tallgrass && block != Blocks.deadbush && !block.isReplaceable(world, pos)) {
            pos = pos.offset(side);
        }

        if (player != null && !player.canPlayerEdit(pos, side, stack))
            return false;

        boolean success = placeTrack(stack, world, pos, side);
        if (success) {
            Block blockTrack = RailcraftBlocks.getBlockTrack();
            IBlockState state = world.getBlockState(pos);
            if (player != null)
                blockTrack.onBlockPlacedBy(world, pos, state, player, stack);
            stack.stackSize--;
        }
        return success;
    }

}
