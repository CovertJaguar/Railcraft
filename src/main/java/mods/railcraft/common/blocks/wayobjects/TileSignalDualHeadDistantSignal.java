/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2016
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.blocks.wayobjects;

import mods.railcraft.api.signals.*;
import mods.railcraft.client.render.tools.RenderTools;
import mods.railcraft.common.util.misc.AABBFactory;
import mods.railcraft.common.util.misc.Game;
import mods.railcraft.common.util.network.RailcraftInputStream;
import mods.railcraft.common.util.network.RailcraftOutputStream;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;

public class TileSignalDualHeadDistantSignal extends TileSignalBase implements IReceiverTile, IDualHeadSignal {

    private static final float SIZE = -0.15f;
    private static final AxisAlignedBB BOUNDING_BOX = AABBFactory.start().box().expandHorizontally(SIZE).build();
    private final DualSignalReceiver receiver = new DualSignalReceiver(getLocalizationTag(), this);

    @Override
    public EnumWayObject getSignalType() {
        return EnumWayObject.DUAL_HEAD_DISTANT_SIGNAL;
    }

    @Override
    public int getLightValue() {
        return Math.max(getSignalAspect(DualLamp.TOP).getLightValue(), getSignalAspect(DualLamp.BOTTOM).getLightValue());
    }

    @Override
    public void update() {
        super.update();
        if (Game.isClient(world)) {
            receiver.tickClient();
            return;
        }

        // TODO: WTF?
        receiver.tickServer();
        int numPairs = receiver.getNumPairs();
        boolean changed = false;
        switch (numPairs) {
            case 0:
                changed |= receiver.setAspect(DualLamp.TOP, SignalAspect.BLINK_RED);
            case 1:
                changed |= receiver.setAspect(DualLamp.BOTTOM, SignalAspect.BLINK_RED);
        }
        if (changed) {
            sendUpdateToClient();
        }
    }

    @Override
    public void onControllerAspectChange(SignalController con, SignalAspect aspect) {
        sendUpdateToClient();
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockAccess world, BlockPos pos) {
        return BOUNDING_BOX;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);

        receiver.writeToNBT(data);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);

        receiver.readFromNBT(data);

    }

    @Override
    public void writePacketData(RailcraftOutputStream data) throws IOException {
        super.writePacketData(data);

        receiver.writePacketData(data);
    }

    @Override
    public void readPacketData(RailcraftInputStream data) throws IOException {
        super.readPacketData(data);

        receiver.readPacketData(data);
    }

    @Override
    public DualSignalReceiver getReceiver() {
        return receiver;
    }

    @Override
    public SignalAspect getSignalAspect(DualLamp lamp) {
        return receiver.getAspect(lamp);
    }

    @Override
    public SignalAspect getSignalAspect() {
        return receiver.getAspect(DualLamp.TOP);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public TextureAtlasSprite getLampTexture(DualLamp lamp, SignalAspect aspect) {
        return RenderTools.getMissingTexture();
    }
}
