package com.colony.mod.network;

import com.colony.mod.ColonyMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Registers all Colony network packets with the NeoForge payload handler system.
 *
 * <p>Server-bound packets are registered with {@code playToServer}; client-bound packets
 * with {@code playToClient}. The version string {@code "1"} must match on both sides.
 */
@EventBusSubscriber(modid = ColonyMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class ColonyNetworking {

    private ColonyNetworking() {}

    @SubscribeEvent
    public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        // Phase 5 — Player employment application (client → server)
        registrar.playToServer(
                PlayerJobApplicationPacket.TYPE,
                PlayerJobApplicationPacket.STREAM_CODEC,
                PlayerJobApplicationPacket::handle
        );

        // Phase 7 — Colonist inspection data (server → client)
        registrar.playToClient(
                ColonistInspectPacket.TYPE,
                ColonistInspectPacket.STREAM_CODEC,
                ColonistInspectPacket::handle
        );

        // Phase 7 — Town ledger query (client → server)
        registrar.playToServer(
                TownLedgerQueryPacket.TYPE,
                TownLedgerQueryPacket.STREAM_CODEC,
                TownLedgerQueryPacket::handle
        );

        // Phase 7 — Town ledger response (server → client)
        registrar.playToClient(
                TownLedgerResponsePacket.TYPE,
                TownLedgerResponsePacket.STREAM_CODEC,
                TownLedgerResponsePacket::handle
        );
    }
}
