package com.colony.mod.network;

import com.colony.mod.ColonyMod;
import com.colony.mod.client.ColonistInspectHud;
import io.netty.buffer.ByteBuf;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.ClientPlayNetworking;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Client-bound packet containing inspection data for a colonist.
 *
 * <p>Sent when the player right-clicks a colonist. The client renders this data as a
 * translucent HUD overlay via {@link ColonistInspectHud}.
 */
public record ColonistInspectPacket(
        String currentGoal,
        float hungerValue,
        float energyValue,
        float socialValue,
        float safetyValue,
        List<RelationshipEntry> topRelationships
) implements CustomPacketPayload {

    public static final Type<ColonistInspectPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ColonyMod.MOD_ID, "colonist_inspect"));

    /** A single entry in the top-relationships list. */
    public record RelationshipEntry(String name, float affinity) {}

    public static final StreamCodec<ByteBuf, RelationshipEntry> RELATIONSHIP_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, RelationshipEntry::name,
                    ByteBufCodecs.FLOAT, RelationshipEntry::affinity,
                    RelationshipEntry::new
            );

    public static final StreamCodec<ByteBuf, ColonistInspectPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, ColonistInspectPacket::currentGoal,
                    ByteBufCodecs.FLOAT, ColonistInspectPacket::hungerValue,
                    ByteBufCodecs.FLOAT, ColonistInspectPacket::energyValue,
                    ByteBufCodecs.FLOAT, ColonistInspectPacket::socialValue,
                    ByteBufCodecs.FLOAT, ColonistInspectPacket::safetyValue,
                    ByteBufCodecs.collection(ArrayList::new, RELATIONSHIP_CODEC), ColonistInspectPacket::topRelationships,
                    ColonistInspectPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Registers the client-side handler. Called from the client entry point.
     */
    @Environment(EnvType.CLIENT)
    public static void registerClientHandler() {
        ClientPlayNetworking.registerGlobalReceiver(TYPE,
                (packet, context) -> context.client().execute(
                        () -> ColonistInspectHud.showInspectData(packet)));
    }
}
