/*
 * Copyright (c) CovertJaguar, 2011-2017
 * http://railcraft.info
 *
 * This code is the property of CovertJaguar
 * and may only be used with explicit written
 * permission unless otherwise specified on the
 * license page at http://railcraft.info/wiki/info:license.
 */
package mods.railcraft.client.particles;

import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleChimney extends ParticleBaseSmoke {

    public ParticleChimney(World par1World, Vec3d start) {
        this(par1World, start, new Vec3d(0, 0, 0), 3f);
    }

    public ParticleChimney(World par1World, Vec3d start, Vec3d vel, float scale) {
        super(par1World, start, vel, scale);
        this.particleGravity = ParticleHelper.SMOKE_GRAVITY;
        this.particleRed = this.particleGreen = this.particleBlue = (float) (Math.random() * 0.3);
        this.particleMaxAge = (int) (24.0D / (Math.random() * 0.5D + 0.2D));
        this.particleMaxAge = (int) (particleMaxAge * scale);
    }

}
