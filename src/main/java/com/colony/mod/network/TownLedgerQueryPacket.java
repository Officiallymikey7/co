package com.colony.mod.network;

import com.colony.mod.ColonyMod;
import com.colony.mod.town.TownManager;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Server-bound packet: a client requests the current state of the Town Ledger.
 *
 * <p>The server responds with a {@link TownLedgerResponsePacket} containing population,
 * treasury, build projects, crime log, and current tax rate.
 */
public record TownLedgerQueryPacket(boolean dummy) implements CustomPacketPayload {

    public static final Type<TownLedgerQueryPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ColonyMod.MOD_ID, "town_ledger_query"));

    public static final StreamCodec<ByteBuf, TownLedgerQueryPacket> STREAM_CODEC =
            ByteBufCodecs.BOOL.map(TownLedgerQueryPacket::new, TownLedgerQueryPacket::dummy);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(TownLedgerQueryPacket packet, ServerPlayNetworking.Context context) {
        context.server().execute(() -> {
            ServerPlayer player = context.player();
            ServerLevel level = player.serverLevel();
            TownManager manager = TownManager.get(level);
            if (manager == null) return;

            TownLedgerResponsePacket response = TownLedgerResponsePacket.from(manager);
            ServerPlayNetworking.send(player, response);
        });
    }
}
