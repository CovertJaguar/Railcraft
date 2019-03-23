/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2019
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.blocks.aesthetics.post;

import mods.railcraft.common.blocks.ItemBlockRailcraftColored;
import mods.railcraft.common.blocks.RailcraftBlocks;
import mods.railcraft.common.plugins.color.EnumColor;
import mods.railcraft.common.plugins.forge.LocalizationPlugin;
import mods.railcraft.common.plugins.forge.WorldPlugin;
import mods.railcraft.common.util.misc.Game;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCauldron;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;

/**
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class ItemPostMetal extends ItemBlockRailcraftColored<BlockPostMetalBase> {

    public ItemPostMetal(BlockPostMetalBase block) {
        super(block);
    }

    @Override
    public void defineRecipes() {
        super.defineRecipes();
        if (RailcraftBlocks.POST.isLoaded())
            MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        if (stack.getItemDamage() == -1 || stack.getItemDamage() == OreDictionary.WILDCARD_VALUE)
            return EnumPost.METAL_UNPAINTED.getTag();
        return super.getTranslationKey() + "." + LocalizationPlugin.convertTag(EnumColor.fromOrdinal(stack.getItemDamage()).getBaseTag());
    }

    /**
     * Washes the dye color from the post or platform by using water in a cauldron.
     *
     * @param event the block interaction event
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onCauldronRightClick(RightClickBlock event) {
        if (event.getUseBlock() == Result.DENY)
            return;

        EntityPlayer player = event.getEntityPlayer();
        World world = player.world;
        if (Game.isClient(world))
            return;

        EnumHand hand = event.getHand();
        ItemStack held = player.getHeldItem(hand);
        if (held.getItem() != this)
            return;

        BlockPos pos = event.getPos();
        IBlockState state = WorldPlugin.getBlockState(world, event.getPos());
        Block cauldron = state.getBlock();
        if (!(cauldron instanceof BlockCauldron)) // Modded cauldrons?
            return;

        int level = state.getValue(BlockCauldron.LEVEL);
        if (level <= 0)
            return;

        ((BlockCauldron) cauldron).setWaterLevel(world, pos, state, level - 1);
        player.setHeldItem(hand, RailcraftBlocks.POST.getStack(block.getUnpaintedType()));
    }
}
