package com.colony.mod.client;

import com.colony.mod.entity.ColonistEntity;
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
 * A dedicated colonist texture ({@code colony:textures/entity/colonist.png}) is
 * applied instead of the zombie skin.
 */
public class ColonistRenderer extends HumanoidMobRenderer<ColonistEntity, HumanoidModel<ColonistEntity>> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("colony", "textures/entity/colonist.png");

    public ColonistRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(ColonistEntity entity) {
        return TEXTURE;
    }
}
