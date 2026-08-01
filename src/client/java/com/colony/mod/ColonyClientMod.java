package com.colony.mod;

import com.colony.mod.client.ColonistInspectHud;
import com.colony.mod.network.ColonyClientNetworking;
import net.fabricmc.api.ClientModInitializer;
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
        // Register client-side networking handlers (server → client packets)
        ColonyClientNetworking.registerClient();

        // Register HUD overlay for colonist inspector
        HudRenderCallback.EVENT.register(ColonistInspectHud::onHudRender);
    }
}
