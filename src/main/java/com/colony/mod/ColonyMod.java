package com.colony.mod;

import com.colony.mod.performance.ColonyAIExecutor;
import com.colony.mod.registry.ColonyBlockEntityTypes;
import com.colony.mod.registry.ColonyEntityTypes;
import com.colony.mod.registry.ColonyBlocks;
import com.colony.mod.registry.ColonyItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
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
@Mod(ColonyMod.MOD_ID)
@EventBusSubscriber(modid = ColonyMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class ColonyMod {

    public static final String MOD_ID = "colony";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public ColonyMod(IEventBus modEventBus, ModContainer modContainer) {
        // Register deferred registries onto the mod event bus
        ColonyEntityTypes.ENTITY_TYPES.register(modEventBus);
        ColonyBlocks.BLOCKS.register(modEventBus);
        ColonyItems.ITEMS.register(modEventBus);
        ColonyBlockEntityTypes.BLOCK_ENTITY_TYPES.register(modEventBus);

        // Register the TOML config (common = shared between client and server)
        modContainer.registerConfig(ModConfig.Type.COMMON, ColonyConfig.SPEC, "colony-common.toml");

        // Common setup (attribute registration, smart-object API built-in seeding, etc.)
        modEventBus.addListener(this::commonSetup);

        LOGGER.info("[Colony] Mod initialising — autonomous colony simulation loading.");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            seedBuiltInSmartObjects();
            LOGGER.info("[Colony] Common setup complete. {} smart-object definitions registered.",
                    com.colony.mod.smartobject.ColonySmartObjectAPI.size());
        });
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
    }

    // -------------------------------------------------------------------------
    // Server lifecycle
    // -------------------------------------------------------------------------

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        ColonyAIExecutor.shutdown();
        LOGGER.info("[Colony] Server stopping — AI executor shut down.");
    }
}
