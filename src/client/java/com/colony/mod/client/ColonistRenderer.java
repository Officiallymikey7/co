package com.colony.mod.client;

import com.colony.mod.ColonyClientMod;
import com.colony.mod.ColonyMod;
import com.colony.mod.entity.ColonistEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Client-side renderer for {@link ColonistEntity}.
 *
 * <p>Uses the custom {@link ColonistModel} baked from the
 * {@link ColonyClientMod#COLONIST_LAYER} model layer registered during client init.
 * A dedicated colonist texture ({@code colony:textures/entity/colonist.png}) is applied.
 */
public class ColonistRenderer extends HumanoidMobRenderer<ColonistEntity, ColonistModel> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ColonyMod.MOD_ID, "textures/entity/colonist.png");

    public ColonistRenderer(EntityRendererProvider.Context context) {
        super(context, new ColonistModel(context.bakeLayer(ColonyClientMod.COLONIST_LAYER)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(ColonistEntity entity) {
        return TEXTURE;
    }
}
