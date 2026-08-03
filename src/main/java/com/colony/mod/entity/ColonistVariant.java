package com.colony.mod.entity;

import com.colony.mod.ColonyMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Canonical list of supported colonist texture variants.
 */
public enum ColonistVariant {
    DEFAULT("colonist", "colonist.png"),
    ASCENDED("ascended", "ascended.png"),
    ASCENDED_2("ascended_2", "ascended2.png"),
    ASCENDED_3("ascended_3", "ascended3.png"),
    ENDERIAN_2("enderian_2", "enderian2.png"),
    ENDERIAN_3("enderian_3", "enderian3.png"),
    ENGINEER_2("engineer_2", "engineer2.png"),
    ENGINEER_3("engineer_3", "engineer3.png"),
    FLORIST_2("florist_2", "florist2.png"),
    FLORIST_3("florist_3", "florist3.png"),
    HUNTER_2("hunter_2", "hunter2.png"),
    HUNTER_3("hunter_3", "hunter3.png"),
    ICEMAN("iceman", "iceman.png"),
    ICEMAN_2("iceman_2", "iceman2.png"),
    ICEMAN_3("iceman_3", "iceman3.png"),
    MINER_2("miner_2", "miner2.png"),
    MINER_3("miner_3", "miner3.png"),
    MINER_VILLAGER_2("miner_villager_2", "miner_villager2.png"),
    MINER_VILLAGER_3("miner_villager_3", "miner_villager3.png"),
    NETHERIAN_2("netherian_2", "netherian2.png"),
    NETHERIAN_3("netherian_3", "netherian3.png"),
    OCEANOGRAPHER_2("oceanographer_2", "oceanographer2.png"),
    OCEANOGRAPHER_3("oceanographer_3", "oceanographer3.png"),
    WOODWORKER_2("woodworker_2", "woodworker2.png"),
    WOODWORKER_3("woodworker_3", "woodworker3.png");

    private static final ColonistVariant[] VALUES = values();
    private static final Map<String, ColonistVariant> BY_ID = Arrays.stream(VALUES)
            .collect(Collectors.toMap(ColonistVariant::id, Function.identity()));

    private final String id;
    private final ResourceLocation textureLocation;
    private final ResourceLocation emissiveTextureLocation;

    ColonistVariant(String id, String textureFileName) {
        this.id = id;
        this.textureLocation = ResourceLocation.fromNamespaceAndPath(
                ColonyMod.MOD_ID, "textures/entity/" + textureFileName);
        this.emissiveTextureLocation = ResourceLocation.fromNamespaceAndPath(
                ColonyMod.MOD_ID, "textures/entity/" + textureFileName.replace(".png", "_eyes.png"));
    }

    public String id() {
        return id;
    }

    public ResourceLocation textureLocation() {
        return textureLocation;
    }

    public ResourceLocation emissiveTextureLocation() {
        return emissiveTextureLocation;
    }

    public static ColonistVariant fromId(String id) {
        if (id == null || id.isBlank()) return DEFAULT;
        return BY_ID.getOrDefault(id.toLowerCase(java.util.Locale.ROOT), DEFAULT);
    }

    public static ColonistVariant random(RandomSource random) {
        return VALUES[random.nextInt(VALUES.length)];
    }

    public static String allIdsCsv() {
        return Arrays.stream(VALUES).map(ColonistVariant::id).collect(Collectors.joining(", "));
    }
}
