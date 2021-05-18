/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2021
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/

package mods.railcraft.common.blocks.single;

import mods.railcraft.common.blocks.BlockEntityDelegate;
import mods.railcraft.common.blocks.ISmartTile;
import mods.railcraft.common.blocks.TileRailcraft;
import mods.railcraft.common.util.effects.HostEffects;
import mods.railcraft.common.util.misc.MathTools;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleDigging;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static net.minecraft.util.EnumFacing.AxisDirection.POSITIVE;

public abstract class BlockAnimated<T extends TileRailcraft & ISmartTile> extends BlockEntityDelegate<T> {

    protected BlockAnimated(Material materialIn) {
        super(materialIn);
    }

    @Override
    public void initializeClient() {
        super.initializeClient();
        //noinspection ConstantConditions
        Item.getItemFromBlock(this).setTileEntityItemStackRenderer(new TileEntityItemStackRenderer() {
            private final T template = createTileEntity(null, getDefaultState());

            @Override
            @SideOnly(Side.CLIENT)
            public void renderByItem(ItemStack p_192838_1_, float partialTicks) {
                TileEntityRendererDispatcher.instance.render(template, 0.0D, 0.0D, 0.0D, 0.0F, partialTicks);
            }
        });
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getLightOpacity(IBlockState state) {
        return 0;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @SideOnly(Side.CLIENT)
    private void configureParticle(@Nullable ParticleDigging digging, BlockPos pos) {
        if (digging == null)
            return;
        digging.setBlockPos(pos);
        ResourceLocation name = Objects.requireNonNull(getRegistryName());
        digging.setParticleTexture(Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(name.getNamespace() + ":blocks/" + name.getPath()));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean addDestroyEffects(World world, BlockPos pos, ParticleManager manager) {
        for (int j = 0; j < 4; ++j) {
            for (int k = 0; k < 4; ++k) {
                for (int l = 0; l < 4; ++l) {
                    double d0 = ((double) j + 0.5D) / 4.0D;
                    double d1 = ((double) k + 0.5D) / 4.0D;
                    double d2 = ((double) l + 0.5D) / 4.0D;
                    ParticleDigging digging = (ParticleDigging) manager.spawnEffectParticle(EnumParticleTypes.BLOCK_CRACK.getParticleID(), (double) pos.getX() + d0, (double) pos.getY() + d1, (double) pos.getZ() + d2, d0 - 0.5D, d1 - 0.5D, d2 - 0.5D, Block.getStateId(getDefaultState()));
                    configureParticle(digging, pos);
                }
            }
        }

        return true;
    }

    @Override
    public boolean addLandingEffects(IBlockState state, WorldServer world, BlockPos pos, IBlockState iblockstate, EntityLivingBase entity, int count) {
        //block dust
        final double speed = 0.15d;
        final ResourceLocation oldId = Objects.requireNonNull(getRegistryName());
        final String id = oldId.getNamespace() + ":blocks/" + oldId.getPath();
        final Vec3d posVec = new Vec3d(pos);
        for (int i = 0; i < count; i++) {
            Vec3d velocity = new Vec3d(MathTools.gaussian() * speed, MathTools.gaussian() * speed, MathTools.gaussian() * speed);
            HostEffects.INSTANCE.blockDust(world, pos, posVec, velocity, state, id);
        }
        return true;
    }

    @Override
    public boolean addRunningEffects(IBlockState state, World world, BlockPos pos, Entity entity) {
        //block crack
        final ResourceLocation oldId = Objects.requireNonNull(getRegistryName());
        final String id = oldId.getNamespace() + ":blocks/" + oldId.getPath();
        final Vec3d velocity = new Vec3d(-entity.motionX * 4.0D, 1.5D, -entity.motionZ * 4.0D);
        final Vec3d position = new Vec3d(entity.posX + ((double) MathTools.nextFloat() - 0.5D) * (double)entity.width, entity.getEntityBoundingBox().minY + 0.1D, entity.posZ + ((double)MathTools.nextFloat() - 0.5D) * (double)entity.width);
        HostEffects.INSTANCE.blockCrack(world, pos, position, velocity, state, id);
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean addHitEffects(IBlockState state, World world, RayTraceResult target, ParticleManager manager) {
        EnumFacing side = target.sideHit;
        BlockPos pos = target.getBlockPos();

        if (state.getRenderType() != EnumBlockRenderType.INVISIBLE) {
            int i = pos.getX();
            int j = pos.getY();
            int k = pos.getZ();
            float f = 0.1F;
            AxisAlignedBB axisalignedbb = state.getBoundingBox(world, pos);
            double d0 = (double) i + Math.random() * (axisalignedbb.maxX - axisalignedbb.minX - 0.20000000298023224D) + 0.10000000149011612D + axisalignedbb.minX;
            double d1 = (double) j + Math.random() * (axisalignedbb.maxY - axisalignedbb.minY - 0.20000000298023224D) + 0.10000000149011612D + axisalignedbb.minY;
            double d2 = (double) k + Math.random() * (axisalignedbb.maxZ - axisalignedbb.minZ - 0.20000000298023224D) + 0.10000000149011612D + axisalignedbb.minZ;

            EnumFacing.AxisDirection direction = side.getAxisDirection();
            switch (side.getAxis()) {
                case X:
                    d0 = i + (direction == POSITIVE ? axisalignedbb.maxX : axisalignedbb.minX) + 0.10000000149011612D * direction.getOffset();
                    break;
                case Y:
                    d1 = j + (direction == POSITIVE ? axisalignedbb.maxY : axisalignedbb.minY) + 0.10000000149011612D * direction.getOffset();
                    break;
                case Z:
                    d2 = k + (direction == POSITIVE ? axisalignedbb.maxZ : axisalignedbb.minZ) + 0.10000000149011612D * direction.getOffset();
                    break;
            }

            ParticleDigging digging = (ParticleDigging) manager.spawnEffectParticle(EnumParticleTypes.BLOCK_CRACK.getParticleID(), d0, d1, d2, 0, 0, 0, Block.getStateId(state));

            configureParticle(digging, pos);

            if (digging != null) {
                digging.multiplyVelocity(0.2F);
                digging.multipleParticleScaleBy(0.6F);
            }
        }

        return true;
    }
}
