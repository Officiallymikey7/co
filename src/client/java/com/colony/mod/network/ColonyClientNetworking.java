package com.colony.mod.network;

import com.colony.mod.client.ColonistInspectHud;
import com.colony.mod.client.TownLedgerScreen;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

/**
 * Registers all client-side networking receivers for the Colony mod.
 *
 * <p>Called from {@link com.colony.mod.ColonyClientMod#onInitializeClient()}.
 * Kept in the client source set so that client-only classes like
 * {@link ClientPlayNetworking} are always available at compile time.
 */
public final class ColonyClientNetworking {

    private ColonyClientNetworking() {}

    /**
     * Registers client-side handlers for server → client packets.
     */
    public static void registerClient() {
        // Colonist inspector overlay
        ClientPlayNetworking.registerGlobalReceiver(ColonistInspectPacket.TYPE,
                (packet, context) -> context.client().execute(
                        () -> ColonistInspectHud.showInspectData(packet)));

        // Town Ledger screen
        ClientPlayNetworking.registerGlobalReceiver(TownLedgerResponsePacket.TYPE,
                (packet, context) -> context.client().execute(
                        () -> handleTownLedger(packet)));
    }

    private static void handleTownLedger(TownLedgerResponsePacket packet) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof TownLedgerScreen ledgerScreen) {
            ledgerScreen.refresh(packet);
        } else {
            mc.setScreen(new TownLedgerScreen(packet));
        }
    }
}
