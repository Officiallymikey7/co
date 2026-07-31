package com.colony.mod;

import com.colony.mod.registry.ColonyEntityTypes;
import com.colony.mod.registry.ColonyBlocks;
import com.colony.mod.registry.ColonyItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
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
 * </ul>
 */
@Mod(ColonyMod.MOD_ID)
public class ColonyMod {

    public static final String MOD_ID = "colony";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public ColonyMod(IEventBus modEventBus, ModContainer modContainer) {
        // Register deferred registries onto the mod event bus
        ColonyEntityTypes.ENTITY_TYPES.register(modEventBus);
        ColonyBlocks.BLOCKS.register(modEventBus);
        ColonyItems.ITEMS.register(modEventBus);

        // Common setup (attribute registration, etc.)
        modEventBus.addListener(this::commonSetup);

        LOGGER.info("[Colony] Mod initialising — autonomous colony simulation loading.");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            LOGGER.info("[Colony] Common setup complete.");
        });
    }
}
