package com.colony.mod.registry;

import com.colony.mod.ColonyMod;
import com.colony.mod.entity.ColonistEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Deferred registration of all custom {@link EntityType}s for the Colony mod.
 */
public final class ColonyEntityTypes {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, ColonyMod.MOD_ID);

    /** The main colonist NPC — the autonomous citizen of the colony. */
    public static final DeferredHolder<EntityType<?>, EntityType<ColonistEntity>> COLONIST =
            ENTITY_TYPES.register("colonist",
            () -> EntityType.Builder.<ColonistEntity>of(ColonistEntity::new, MobCategory.CREATURE)
                    .sized(0.6f, 1.95f)
                    .clientTrackingRange(10)
                    .build("colonist"));

    private ColonyEntityTypes() {}
}
