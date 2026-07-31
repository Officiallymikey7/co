package com.colony.mod.network;

import com.colony.mod.ColonyMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/**
 * Registers all Colony network packets with the Fabric networking API.
 *
 * <p>Server-bound packets are registered with {@code playC2S}; client-bound packets
 * with {@code playS2C}. Server-side handlers are registered here; client-side handlers
 * are registered in {@link #registerClient()} (called from the client entry point).
 */
public final class ColonyNetworking {

    private ColonyNetworking() {}

    /**
     * Registers payload types and server-side handlers. Called during common (main) init.
     */
    public static void registerCommon() {
        // Client-bound payload types (server → client)
        PayloadTypeRegistry.playS2C().register(ColonistInspectPacket.TYPE, ColonistInspectPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(TownLedgerResponsePacket.TYPE, TownLedgerResponsePacket.STREAM_CODEC);

        // Server-bound payload types (client → server)
        PayloadTypeRegistry.playC2S().register(PlayerJobApplicationPacket.TYPE, PlayerJobApplicationPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(TownLedgerQueryPacket.TYPE, TownLedgerQueryPacket.STREAM_CODEC);

        // Server-side handlers for client → server packets
        ServerPlayNetworking.registerGlobalReceiver(PlayerJobApplicationPacket.TYPE,
                PlayerJobApplicationPacket::handle);
        ServerPlayNetworking.registerGlobalReceiver(TownLedgerQueryPacket.TYPE,
                TownLedgerQueryPacket::handle);
    }

    /**
     * Registers client-side handlers for server → client packets. Called during client init.
     */
    @Environment(EnvType.CLIENT)
    public static void registerClient() {
        ColonistInspectPacket.registerClientHandler();
        TownLedgerResponsePacket.registerClientHandler();
    }
}
