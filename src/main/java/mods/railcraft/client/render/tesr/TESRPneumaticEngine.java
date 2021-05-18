/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2019
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.client.render.tesr;

import mods.railcraft.client.render.models.programmatic.engine.ModelEngineBase;
import mods.railcraft.client.render.models.programmatic.engine.ModelEngineFrame;
import mods.railcraft.client.render.models.programmatic.engine.ModelEnginePiston;
import mods.railcraft.client.render.models.programmatic.engine.ModelEngineTrunk;
import mods.railcraft.client.render.tools.OpenGL;
import mods.railcraft.common.blocks.single.TileEngine;
import mods.railcraft.common.blocks.single.TileEngine.EnergyStage;
import mods.railcraft.common.core.RailcraftConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

public final class TESRPneumaticEngine extends TileEntitySpecialRenderer<TileEngine> {

    private static final float[] angleMap = new float[6];
    private static final ModelEngineFrame frame = new ModelEngineFrame();
    private static final ModelEngineBase base = new ModelEngineBase();
    private static final ModelEngineTrunk trunk = new ModelEngineTrunk();
    private static final ModelEnginePiston piston = new ModelEnginePiston();
    private final ResourceLocation texture;

    static {
        angleMap[EnumFacing.EAST.ordinal()] = (float) -Math.PI / 2;
        angleMap[EnumFacing.WEST.ordinal()] = (float) Math.PI / 2;
        angleMap[EnumFacing.UP.ordinal()] = 0;
        angleMap[EnumFacing.DOWN.ordinal()] = (float) Math.PI;
        angleMap[EnumFacing.SOUTH.ordinal()] = (float) Math.PI / 2;
        angleMap[EnumFacing.NORTH.ordinal()] = (float) -Math.PI / 2;
    }

    public TESRPneumaticEngine(String tag) {
        this.texture = new ResourceLocation(RailcraftConstants.TESR_TEXTURE_FOLDER + tag + ".png");
    }

    @Override
    public void render(TileEngine engine, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        render(engine.getEnergyStage(), engine.getProgress(), engine.getFacing(), x, y, z);
    }

    private void render(EnergyStage stage, float progress, EnumFacing orientation, double x, double y, double z) {
        GlStateManager.color(1, 1, 1);
        GlStateManager.pushMatrix();

        GlStateManager.translate((float) x, (float) y, (float) z);

        float[] angle = {0, 0, 0};
        float[] translate = {orientation.getXOffset(), orientation.getYOffset(), orientation.getZOffset()};

        switch (orientation) {
            case EAST:
            case WEST:
            case DOWN:
                angle[2] = angleMap[orientation.ordinal()];
                break;
            case SOUTH:
            case NORTH:
                angle[0] = angleMap[orientation.ordinal()];
                break;
        }

        base.setRotation(angle[0], angle[1], angle[2]);
        trunk.rotate(angle[0], angle[1], angle[2]);
        frame.setRotation(angle[0], angle[1], angle[2]);
        piston.setRotation(angle[0], angle[1], angle[2]);

        float factor = (float) (1.0 / 16.0);
        bindTexture(texture);

        trunk.render(stage, factor);
        base.render(factor);

        float step;
        if (progress > 0.5) {
            step = 7.99F - (progress - 0.5F) * 2F * 7.99F;
        } else {
            step = progress * 2F * 7.99F;
        }
        float frameTrans = step / 16;
        GlStateManager.translate(translate[0] * frameTrans, translate[1] * frameTrans, translate[2] * frameTrans);
        frame.render(factor);
        GlStateManager.translate(-translate[0] * frameTrans, -translate[1] * frameTrans, -translate[2] * frameTrans);

        float pistonPrep = 0.01f;
        GlStateManager.translate(-translate[0] * pistonPrep, -translate[1] * pistonPrep, -translate[2] * pistonPrep);

        float pistonTrans = 2F / 16F;

        for (int i = 0; i <= step + 2; i += 2) {
            piston.render(factor);
            GlStateManager.translate(translate[0] * pistonTrans, translate[1] * pistonTrans, translate[2] * pistonTrans);
        }

        GlStateManager.popMatrix();
    }
}
