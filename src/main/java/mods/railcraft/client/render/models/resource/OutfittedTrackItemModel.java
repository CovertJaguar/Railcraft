/*
 * Copyright (c) CovertJaguar, 2011-2017
 * http://railcraft.info
 *
 * This code is the property of CovertJaguar
 * and may only be used with explicit written
 * permission unless otherwise specified on the
 * license page at http://railcraft.info/wiki/info:license.
 */
package mods.railcraft.client.render.models.resource;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import mods.railcraft.api.tracks.TrackKit;
import mods.railcraft.api.tracks.TrackRegistry;
import mods.railcraft.api.tracks.TrackType;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.IRetexturableModel;
import net.minecraftforge.client.model.ItemLayerModel;
import net.minecraftforge.common.model.IModelState;

import java.io.IOException;
import java.util.Collection;
import java.util.Objects;

import static mods.railcraft.common.blocks.tracks.outfitted.ItemTrackOutfitted.MODEL_PREFIX;

/**
 * Created by CovertJaguar on 8/18/2016 for Railcraft.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class OutfittedTrackItemModel implements IRetexturableModel {
    private final ItemLayerModel model;

    public OutfittedTrackItemModel(ImmutableList<ResourceLocation> textures) {
        this.model = new ItemLayerModel(textures);
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return model.getDependencies();
    }

    @Override
    public Collection<ResourceLocation> getTextures() {
        return model.getTextures();
    }

    @Override
    public IModelState getDefaultState() {
        return Transforms.getItem();
    }

    @Override
    public ItemLayerModel retexture(ImmutableMap<String, String> textures) {
        return model.retexture(textures);
    }

    @SuppressWarnings("Guava")
    @Override
    public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        return model.bake(state, format, bakedTextureGetter);
    }

    public enum Loader implements ICustomModelLoader {
        INSTANCE {
            @Override
            public void onResourceManagerReload(IResourceManager resourceManager) {
            }

            @Override
            public boolean accepts(ResourceLocation modelLocation) {
                return Objects.equals(modelLocation.getResourceDomain(), "railcraft")
                        && modelLocation.getResourcePath().startsWith(MODEL_PREFIX);
            }

            @Override
            public IModel loadModel(ResourceLocation modelLocation) throws IOException {
                String[] tokens = modelLocation.getResourcePath().split("\\.");
                TrackType trackType = TrackRegistry.TRACK_TYPE.get(tokens[1]);
                TrackKit trackKit = TrackRegistry.TRACK_KIT.get(tokens[2]);
                ImmutableList.Builder<ResourceLocation> texBuilder = ImmutableList.builder();
                switch (trackKit.getRenderer()) {
                    case COMPOSITE:
                        texBuilder.add(new ResourceLocation(trackType.getRegistryName().getResourceDomain(),
                                "blocks/tracks/outfitted/type/" + trackType.getRegistryName().getResourcePath()));
                        texBuilder.add(new ResourceLocation(trackKit.getRegistryName().getResourceDomain(),
                                "blocks/tracks/outfitted/kit/" + trackKit.getRegistryName().getResourcePath() + "_0"));
                        break;
                    case UNIFIED:
                        // TODO: fix this
                        texBuilder.add(new ResourceLocation(trackType.getRegistryName().getResourceDomain(),
                                "blocks/tracks/outfitted/type/" + trackType.getRegistryName().getResourcePath()));
                        break;
                }
                return new OutfittedTrackItemModel(texBuilder.build());
            }
        }
    }
}
