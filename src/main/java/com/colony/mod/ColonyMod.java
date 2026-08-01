package com.colony.mod;

import com.colony.mod.entity.ColonistEntity;
import com.colony.mod.event.CrimeCommittedEvent;
import com.colony.mod.network.ColonyNetworking;
import com.colony.mod.performance.ColonyAIExecutor;
import com.colony.mod.registry.ColonyBlockEntityTypes;
import com.colony.mod.registry.ColonyBlocks;
import com.colony.mod.registry.ColonyEntityTypes;
import com.colony.mod.registry.ColonyItems;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import com.colony.mod.town.CrimeType;
import com.colony.mod.town.TownManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.levelgen.Heightmap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Colony Mod — A self-governing autonomous colony simulation.
 *
 * <p>In this mod, the player is an equal citizen in a self-managing world. Villagers (colonists)
 * handle their own survival, relationships, job assignments, and town expansion through emergent AI.
 * The mod uses a Utility AI + GOAP (Goal-Oriented Action Planning) engine to drive autonomous
 * behaviour, combined with a macro-level Town Planner that triggers construction when demographic
 * thresholds are met.
 *
 * <p>Architecture overview:
 * <ul>
 *   <li>Needs System — continuous 0–100 stats per colonist (Hunger, Energy, Social, Safety)</li>
 *   <li>Utility AI — dynamic action scoring, highest-score goal wins each tick</li>
 *   <li>GOAP Planner — backward-chains required actions for the selected goal</li>
 *   <li>Smart Objects — blocks/entities advertise utility to nearby colonists</li>
 *   <li>Town Planner — monitors colony metrics and triggers autonomous construction</li>
 *   <li>Social Network — relationship tracking drives cohabitation and town growth</li>
 *   <li>Player Systems — employment, wages, rent, and colony law (Phase 5)</li>
 *   <li>Performance — async AI planning and abstract simulation (Phase 6)</li>
 *   <li>UI — colonist inspector overlay, Town Ledger screen (Phase 7)</li>
 *   <li>Smart Object API — extensible third-party block registration (Phase 8)</li>
 * </ul>
 */
public class ColonyMod implements ModInitializer {

    public static final String MOD_ID = "colony";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // Register all content
        ColonyEntityTypes.register();
        ColonyBlocks.register();
        ColonyItems.register();
        ColonyBlockEntityTypes.register();

        // Register entity attributes
        FabricDefaultAttributeRegistry.register(ColonyEntityTypes.COLONIST, ColonistEntity.createAttributes());
        SpawnPlacements.register(
                ColonyEntityTypes.COLONIST,
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ColonistEntity::checkColonistSpawnRules
        );
        BiomeModifications.addSpawn(
                BiomeSelectors.foundInOverworld(),
                net.minecraft.world.entity.MobCategory.CREATURE,
                ColonyEntityTypes.COLONIST,
                8,
                1,
                3
        );

        // Load config from disk
        ColonyConfig.load();

        // Register networking payload types and server-side handlers
        ColonyNetworking.registerCommon();

        // Seed built-in smart-object definitions
        seedBuiltInSmartObjects();

        // Server lifecycle: shut down AI executor when server stops
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            ColonyAIExecutor.shutdown();
            LOGGER.info("[Colony] Server stopping — AI executor shut down.");
        });

        // Per-level tick: drive the Colony State Monitor
        ServerTickEvents.END_WORLD_TICK.register(level -> {
            if (level instanceof ServerLevel serverLevel) {
                TownManager.get(serverLevel).serverTick(serverLevel);
            }
        });

        // Crime detection: attacking a colonist is assault
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world instanceof ServerLevel serverLevel
                    && player instanceof ServerPlayer
                    && entity instanceof ColonistEntity) {
                TownManager manager = TownManager.get(serverLevel);
                if (manager != null) {
                    manager.handleCrime(new CrimeCommittedEvent(player, CrimeType.ASSAULT));
                }
            }
            return InteractionResult.PASS;
        });

        LOGGER.info("[Colony] Mod initialising — autonomous colony simulation loading.");
    }

    /**
     * Seeds the {@link com.colony.mod.smartobject.ColonySmartObjectAPI} with built-in definitions
     * derived from the legacy {@link com.colony.mod.smartobject.SmartObjectType} enum.
     * This allows the chunk scanner and third-party code to use the same API path.
     */
    private void seedBuiltInSmartObjects() {
        for (com.colony.mod.smartobject.SmartObjectType type
                : com.colony.mod.smartobject.SmartObjectType.values()) {
            // Built-in types don't have a BlockState matcher yet (that requires concrete block
            // registrations). Register a placeholder that always returns false so the API size
            // is correct; concrete matchers will be set when colony blocks are placed.
            com.colony.mod.smartobject.ColonySmartObjectAPI.register(
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MOD_ID, type.name().toLowerCase()),
                    new com.colony.mod.smartobject.SmartObjectDefinition(
                            type.getTargetNeed(),
                            type.getSatisfactionAmount(),
                            type.getUsageDurationTicks(),
                            state -> false // placeholder; real matcher set via block event
                    )
            );
        }
        LOGGER.info("[Colony] Common setup complete. {} smart-object definitions registered.",
                com.colony.mod.smartobject.ColonySmartObjectAPI.size());
    }
}
