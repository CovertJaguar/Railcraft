/*
 * Copyright (c) CovertJaguar, 2011-2017
 * http://railcraft.info
 *
 * This code is the property of CovertJaguar
 * and may only be used with explicit written
 * permission unless otherwise specified on the
 * license page at http://railcraft.info/wiki/info:license.
 */
package mods.railcraft.common.carts;

import com.mojang.authlib.GameProfile;
import mods.railcraft.api.carts.locomotive.LocomotiveModelRenderer;
import mods.railcraft.api.carts.locomotive.LocomotiveRenderType;
import mods.railcraft.client.emblems.Emblem;
import mods.railcraft.client.emblems.EmblemToolsClient;
import mods.railcraft.common.plugins.color.ColorPlugin;
import mods.railcraft.common.plugins.color.EnumColor;
import mods.railcraft.common.plugins.forge.CraftingPlugin;
import mods.railcraft.common.plugins.forge.LocalizationPlugin;
import mods.railcraft.common.plugins.forge.PlayerPlugin;
import mods.railcraft.common.util.inventory.InvTools;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

/**
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class ItemLocomotive extends ItemCart implements ColorPlugin.IColoredItem {

    private final LocomotiveRenderType renderType;
    //    private IIcon blankIcon;
    private final EnumColor defaultPrimary;
    private final EnumColor defaultSecondary;

    public ItemLocomotive(IRailcraftCartContainer cart, LocomotiveRenderType renderType, EnumColor primary, EnumColor secondary) {
        super(cart);
        this.renderType = renderType;
        setMaxStackSize(1);
        this.defaultPrimary = primary;
        this.defaultSecondary = secondary;
    }

    @Override
    public void finalizeDefinition() {
        ColorPlugin.instance.register(this, this);
    }

    @Override
    public void defineRecipes() {
        IRecipe recipe = new LocomotivePaintingRecipe(new ItemStack(this));
        CraftingPlugin.addRecipe(recipe);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(Item item, CreativeTabs tab, List<ItemStack> list) {
        for (String skin : renderType.getRendererTags()) {
            list.add(renderType.getItemWithRenderer(skin, new ItemStack(this)));
        }
    }

//    @Override
//    public void registerIcons(IIconRegister iconRegister) {
//        renderType.registerIcons(iconRegister);
//        blankIcon = iconRegister.registerIcon("railcraft:locomotives/blank");
//    }

    //    @Override
//    public boolean requiresMultipleRenderPasses() {
//        return true;
//    }
//
//    @Override
//    public int getRenderPasses(int metadata) {
//        return 3;
//    }
    @Override
    @SideOnly(Side.CLIENT)
    public IItemColor colorHandler() {
        return (stack, tintIndex) -> {
            switch (tintIndex) {
                case 0:
                    return getPrimaryColor(stack).getHexColor();
                case 1:
                    return getSecondaryColor(stack).getHexColor();
                default:
                    return EnumColor.WHITE.getHexColor();
            }
        };
    }

//    @Override
//    @SideOnly(Side.CLIENT)
//    public IIcon getIcon(ItemStack stack, int pass) {
//        String rendererTag = getModel(stack);
//        LocomotiveModelRenderer renderer = renderType.getRenderer(rendererTag);
//        if (renderer == null)
//            return RenderTools.getMissingTexture();
//        IIcon[] icons = renderer.getItemIcons();
//        if (pass >= icons.length || icons[pass] == null)
//            return blankIcon;
//        return renderer.getItemIcons()[pass];
//    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> info, boolean adv) {
        super.addInformation(stack, player, info, adv);
        GameProfile owner = getOwner(stack);
        if (owner.getName() != null && !owner.getName().equals("[Unknown]")) {
            String format = LocalizationPlugin.translate("gui.railcraft.locomotive.tips.item.owner");
            info.add(String.format(format, owner.getName()));
        }

        String model = getModel(stack);
        LocomotiveModelRenderer renderer = renderType.getRenderer(model);
        String modelName;
        if (renderer != null)
            modelName = renderer.getDisplayName();
        else
            modelName = LocalizationPlugin.translate("gui.railcraft.locomotive.tips.item.model.default");
        String format = LocalizationPlugin.translate("gui.railcraft.locomotive.tips.item.model");
        info.add(String.format(format, modelName));

        EnumColor primary = getPrimaryColor(stack);
        format = LocalizationPlugin.translate("gui.railcraft.locomotive.tips.item.primary");
        info.add(String.format(format, primary.getTranslatedName()));

        EnumColor secondary = getSecondaryColor(stack);
        format = LocalizationPlugin.translate("gui.railcraft.locomotive.tips.item.secondary");
        info.add(String.format(format, secondary.getTranslatedName()));

        float whistle = getWhistlePitch(stack);
        format = LocalizationPlugin.translate("gui.railcraft.locomotive.tips.item.whistle");
        info.add(String.format(format, whistle < 0 ? "???" : String.format("%.2f", whistle)));

        String emblemIdent = getEmblem(stack);
        if (emblemIdent != null && !emblemIdent.isEmpty() && EmblemToolsClient.packageManager != null) {
            Emblem emblem = EmblemToolsClient.packageManager.getEmblem(emblemIdent);
            if (emblem != null) {
                format = LocalizationPlugin.translate("gui.railcraft.locomotive.tips.item.emblem");
                info.add(String.format(format, emblem.displayName));
            }
        }
    }

    public static void setItemColorData(ItemStack stack, EnumDyeColor primaryColor, EnumDyeColor secondaryColor) {
        setItemColorData(stack, EnumColor.fromDye(primaryColor), EnumColor.fromDye(secondaryColor));
    }

    public static void setItemColorData(ItemStack stack, EnumColor primaryColor, EnumColor secondaryColor) {
        primaryColor.setItemColor(stack, "primaryColor");
        secondaryColor.setItemColor(stack, "secondaryColor");
    }

    public static void setItemWhistleData(ItemStack stack, float whistlePitch) {
        NBTTagCompound nbt = InvTools.getItemData(stack);
        nbt.setFloat("whistlePitch", whistlePitch);
    }

    public static float getWhistlePitch(ItemStack stack) {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null || !nbt.hasKey("whistlePitch"))
            return -1;
        return nbt.getFloat("whistlePitch");
    }

    public static void setOwnerData(ItemStack stack, GameProfile owner) {
        NBTTagCompound nbt = InvTools.getItemData(stack);
        PlayerPlugin.writeOwnerToNBT(nbt, owner);
    }

    public static GameProfile getOwner(ItemStack stack) {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null)
            return new GameProfile(null, "[Unknown]");
        return PlayerPlugin.readOwnerFromNBT(nbt);
    }

    public static void setEmblem(ItemStack stack, String emblemIdentifier) {
        NBTTagCompound nbt = InvTools.getItemData(stack);
        nbt.setString("emblem", emblemIdentifier);
    }

    public static String getEmblem(ItemStack stack) {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null || !nbt.hasKey("emblem"))
            return "";
        return nbt.getString("emblem");
    }

    public static void setModel(ItemStack stack, String modelTag) {
        NBTTagCompound nbt = InvTools.getItemData(stack);
        nbt.setString("model", modelTag);
    }

    public static String getModel(ItemStack stack) {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null || !nbt.hasKey("model"))
            return "default";
        return nbt.getString("model");
    }

    public static EnumColor getPrimaryColor(ItemStack stack) {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null || !nbt.hasKey("primaryColor")) {
            return ((ItemLocomotive) stack.getItem()).defaultPrimary;
        }
        return EnumColor.readFromNBT(nbt, "primaryColor");
    }

    public static EnumColor getSecondaryColor(ItemStack stack) {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null || !nbt.hasKey("secondaryColor")) {
            return ((ItemLocomotive) stack.getItem()).defaultSecondary;
        }
        return EnumColor.readFromNBT(nbt, "secondaryColor");
    }

}
