package com.colony.mod.client;

import com.colony.mod.ColonyMod;
import com.colony.mod.client.model.CustomModel;
import com.colony.mod.entity.ColonistEntity;
import com.colony.mod.entity.ColonistVariant;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/**
 * Client-side renderer for {@link ColonistEntity}.
 *
 * <p>Uses the male colony model ({@link CustomModel}) baked via
 * {@link CustomModel#LAYER_LOCATION}, plus an optional emissive overlay pass.
 */
public class ColonistRenderer extends MobRenderer<ColonistEntity, CustomModel> {
    private static final float BASE_SCALE = 0.9375F;
    private static final int FULL_BRIGHT = 15728640;

    private static final ResourceLocation FALLBACK_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ColonyMod.MOD_ID, "textures/entity/colony/male1.png");

    public ColonistRenderer(EntityRendererProvider.Context context) {
        super(context, new CustomModel(context.bakeLayer(CustomModel.LAYER_LOCATION)), 0.5f);
        this.addLayer(new ColonistEmissiveLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(ColonistEntity entity) {
        ColonistVariant variant = entity.getVariant();
        ResourceLocation texture = variant.textureLocation();
        return texture != null ? texture : FALLBACK_TEXTURE;
    }

    @Override
    protected void scale(ColonistEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(BASE_SCALE, BASE_SCALE, BASE_SCALE);
    }

    @Override
    protected RenderType getRenderType(ColonistEntity entity, boolean bodyVisible, boolean translucent, boolean glowing) {
        ResourceLocation texture = this.getTextureLocation(entity);
        if (translucent) {
            return RenderType.itemEntityTranslucentCull(texture);
        }

        if (bodyVisible) {
            return this.model.renderType(texture);
        }

        return glowing ? RenderType.outline(texture) : null;
    }

    private static final class ColonistEmissiveLayer extends RenderLayer<ColonistEntity, CustomModel> {
        private final Map<ResourceLocation, Boolean> textureCache = new HashMap<>();

        private ColonistEmissiveLayer(RenderLayerParent<ColonistEntity, CustomModel> parent) {
            super(parent);
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, ColonistEntity entity,
                           float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                           float netHeadYaw, float headPitch) {
            ResourceLocation emissiveTexture = entity.getVariant().emissiveTextureLocation();
            if (!hasTexture(emissiveTexture)) {
                return;
            }

            VertexConsumer consumer = buffer.getBuffer(RenderType.eyes(emissiveTexture));
            this.getParentModel().renderToBuffer(
                    poseStack,
                    consumer,
                    FULL_BRIGHT,
                    OverlayTexture.NO_OVERLAY,
                    1.0F,
                    1.0F,
                    1.0F,
                    1.0F);
        }

        private boolean hasTexture(ResourceLocation emissiveTexture) {
            if (this.textureCache.containsKey(emissiveTexture)) {
                return this.textureCache.get(emissiveTexture);
            }
            boolean present = Minecraft.getInstance().getResourceManager().getResource(emissiveTexture).isPresent();
            if (present) {
                this.textureCache.put(emissiveTexture, true);
            }
            return present;
        }
    }
}
