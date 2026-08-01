package com.colony.mod.network;

import com.colony.mod.ColonyMod;
import com.colony.mod.town.TownData;
import com.colony.mod.town.TownManager;
import com.colony.mod.town.builder.BuilderTask;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Client-bound packet containing the current Town Ledger data.
 *
 * <p>Sent in response to a {@link TownLedgerQueryPacket}. The client opens or refreshes the
 * Town Ledger screen with this data.
 */
public record TownLedgerResponsePacket(
        String townName,
        int population,
        int treasury,
        double taxRate,
        List<String> activeBuildProjects,
        List<String> recentCrimes
) implements CustomPacketPayload {

    public static final Type<TownLedgerResponsePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ColonyMod.MOD_ID, "town_ledger_response"));

    public static final StreamCodec<ByteBuf, TownLedgerResponsePacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, TownLedgerResponsePacket::townName,
                    ByteBufCodecs.INT, TownLedgerResponsePacket::population,
                    ByteBufCodecs.INT, TownLedgerResponsePacket::treasury,
                    ByteBufCodecs.DOUBLE, TownLedgerResponsePacket::taxRate,
                    ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.STRING_UTF8), TownLedgerResponsePacket::activeBuildProjects,
                    ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.STRING_UTF8), TownLedgerResponsePacket::recentCrimes,
                    TownLedgerResponsePacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /** Builds a response packet from the current server-side state. */
    public static TownLedgerResponsePacket from(TownManager manager) {
        TownData data = manager.getTownData();

        List<String> projects = new ArrayList<>();
        for (BuilderTask task : manager.getStateMonitor().getPendingTasks()) {
            projects.add(task.getBlueprint().getTemplateName() + " [" + task.getStatus().name() + "]");
        }

        List<String> crimes = new ArrayList<>(data.getLawRecord().getRecentCrimes(10));

        return new TownLedgerResponsePacket(
                data.getTownName(),
                data.getPopulation(),
                data.getTownTreasury(),
                data.getLawRecord().getTaxRate(),
                projects,
                crimes
        );
    }
}

