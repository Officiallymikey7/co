package com.colony.mod;

import com.colony.mod.network.ColonyNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import com.colony.mod.client.ColonistInspectHud;

/**
 * Client-side entry point for the Colony mod.
 *
 * <p>Registers client-only event listeners (HUD rendering, client networking handlers).
 * This class is only loaded on the client side.
 */
@Environment(EnvType.CLIENT)
public class ColonyClientMod implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Register client-side networking handlers (server → client packets)
        ColonyNetworking.registerClient();

        // Register HUD overlay for colonist inspector
        HudRenderCallback.EVENT.register(ColonistInspectHud::onHudRender);
    }
}
