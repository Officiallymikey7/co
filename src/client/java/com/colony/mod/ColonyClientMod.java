package com.colony.mod;

import com.colony.mod.client.ColonistInspectHud;
import com.colony.mod.client.ColonistRenderer;
import com.colony.mod.client.model.ColonistModel;
import com.colony.mod.network.ColonyClientNetworking;
import com.colony.mod.registry.ColonyEntityTypes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

/**
 * Client-side entry point for the Colony mod.
 *
 * <p>Registers client-only event listeners (HUD rendering, client networking handlers).
 * This class is only loaded on the client side.
 */
public class ColonyClientMod implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(ColonistModel.LAYER_LOCATION, ColonistModel::createBodyLayer);

        // Register entity renderer — must happen before any colonist is rendered
        EntityRendererRegistry.register(ColonyEntityTypes.COLONIST, ColonistRenderer::new);

        // Register client-side networking handlers (server → client packets)
        ColonyClientNetworking.registerClient();

        // Register HUD overlay for colonist inspector
        HudRenderCallback.EVENT.register(ColonistInspectHud::onHudRender);
    }
}
