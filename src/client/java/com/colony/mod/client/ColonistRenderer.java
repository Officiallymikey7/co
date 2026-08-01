package com.colony.mod.client;

import com.colony.mod.ColonyMod;
import com.colony.mod.entity.ColonistEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Client-side renderer for {@link ColonistEntity}.
 *
 * <p>Uses a dedicated colonist model layer instead of borrowing the vanilla zombie model.
 */
public class ColonistRenderer extends HumanoidMobRenderer<ColonistEntity, ColonistModel> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ColonyMod.MOD_ID, "textures/entity/colonist.png");

    public ColonistRenderer(EntityRendererProvider.Context context) {
        super(context, new ColonistModel(context.bakeLayer(ColonistModel.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(ColonistEntity entity) {
        return TEXTURE;
    }
}
