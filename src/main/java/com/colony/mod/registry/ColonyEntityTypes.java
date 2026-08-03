package com.colony.mod.registry;

import com.colony.mod.ColonyMod;
import com.colony.mod.entity.ColonistEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

/**
 * Registration of all custom {@link EntityType}s for the Colony mod.
 */
public final class ColonyEntityTypes {

    /** The main colonist NPC — the autonomous citizen of the colony. */
    public static EntityType<ColonistEntity> COLONIST;

    public static void register() {
        ResourceKey<EntityType<?>> key = ResourceKey.create(
                Registries.ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath(ColonyMod.MOD_ID, "colonist")
        );
        COLONIST = Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                key,
                EntityType.Builder.<ColonistEntity>of(ColonistEntity::new, MobCategory.CREATURE)
                        .sized(0.6f, 1.95f)
                        .clientTrackingRange(10)
                        .build(key)
        );
    }

    private ColonyEntityTypes() {}
}
