package com.colony.mod.network;

import com.colony.mod.ColonyMod;
import com.colony.mod.town.JobRole;
import com.colony.mod.town.TownManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Locale;

/**
 * Server-bound packet: a player requests a shift at a colony job site.
 *
 * <p>The server validates the request (job vacancy exists, player is a registered colony member)
 * and, if approved, assigns the player the requested role and begins their wage accrual.
 */
public record PlayerJobApplicationPacket(String requestedRole) implements CustomPacketPayload {

    public static final Type<PlayerJobApplicationPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ColonyMod.MOD_ID, "player_job_application"));

    public static final StreamCodec<ByteBuf, PlayerJobApplicationPacket> STREAM_CODEC =
            ByteBufCodecs.STRING_UTF8.map(PlayerJobApplicationPacket::new, PlayerJobApplicationPacket::requestedRole);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Called on the server thread when this packet is received from a client.
     */
    public static void handle(PlayerJobApplicationPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            JobRole role;
            try {
                role = JobRole.valueOf(packet.requestedRole().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                ColonyMod.LOGGER.warn("[Colony] Player {} sent invalid job role: {}",
                        player.getName().getString(), packet.requestedRole());
                return;
            }

            if (role == JobRole.UNEMPLOYED || role == JobRole.PLAYER) {
                ColonyMod.LOGGER.warn("[Colony] Player {} tried to apply for a non-assignable role: {}",
                        player.getName().getString(), role);
                return;
            }

            ServerLevel level = player.serverLevel();
            TownManager manager = TownManager.get(level);
            if (manager == null) return;

            manager.applyPlayerForJob(player, role);
        });
    }
}
