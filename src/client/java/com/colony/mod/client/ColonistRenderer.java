package com.colony.mod.client;

import com.colony.mod.ColonyMod;
import com.colony.mod.entity.ColonistEntity;
import com.colony.mod.entity.ColonistVariant;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Client-side renderer for {@link ColonistEntity}.
 *
 * <p>Re-uses the zombie body {@link ModelLayers#ZOMBIE model layer} so no custom
 * {@link net.minecraft.client.model.geom.ModelLayerLocation} needs to be registered.
 * A colonist variant texture under {@code colony:textures/entity/*} is selected from
 * synced entity data, with {@code colonist.png} used as a fallback.
 */
public class ColonistRenderer extends HumanoidMobRenderer<ColonistEntity, HumanoidModel<ColonistEntity>> {

    private static final ResourceLocation FALLBACK_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ColonyMod.MOD_ID, "textures/entity/colonist.png");

    public ColonistRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(ColonistEntity entity) {
        ColonistVariant variant = entity.getVariant();
        ResourceLocation texture = variant.textureLocation();
        return texture != null ? texture : FALLBACK_TEXTURE;
    }
}
