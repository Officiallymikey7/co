package com.colony.mod;

import com.colony.mod.client.ColonistInspectHud;
import com.colony.mod.client.ColonistModel;
import com.colony.mod.client.ColonistRenderer;
import com.colony.mod.network.ColonyClientNetworking;
import com.colony.mod.registry.ColonyEntityTypes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * Client-side entry point for the Colony mod.
 *
 * <p>Registers client-only event listeners (HUD rendering, client networking handlers).
 * This class is only loaded on the client side.
 */
public class ColonyClientMod implements ClientModInitializer {

    /** Model layer used by {@link ColonistRenderer}. */
    public static final ModelLayerLocation COLONIST_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ColonyMod.MOD_ID, "colonist"), "main");

    @Override
    public void onInitializeClient() {
        // Register colonist model layer before any renderer bakes it
        EntityModelLayerRegistry.registerModelLayer(COLONIST_LAYER, ColonistModel::createBodyLayer);

        // Register entity renderer — must happen before any colonist is rendered
        EntityRendererRegistry.register(ColonyEntityTypes.COLONIST, ColonistRenderer::new);

        // Register client-side networking handlers (server → client packets)
        ColonyClientNetworking.registerClient();

        // Register HUD overlay for colonist inspector
        HudRenderCallback.EVENT.register(ColonistInspectHud::onHudRender);
    }
}
