package com.colony.mod.smartobject;

import com.colony.mod.ColonyMod;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Public API for registering custom Smart Object definitions with the Colony mod.
 *
 * <p>Third-party mods (e.g. Farmer's Delight, Create, furniture mods) call
 * {@link #register(ResourceLocation, SmartObjectDefinition)} during their mod initialisation
 * to teach Colony colonists about their custom blocks. Once registered, the
 * {@link SmartObjectRegistry#scanChunk} method will automatically detect matching blocks
 * and make them available as smart objects for colonist need-satisfaction.
 *
 * <h2>Usage (from another Fabric mod):</h2>
 * <pre>{@code
 * // in your ModInitializer.onInitialize():
 * ColonySmartObjectAPI.register(
 *     ResourceLocation.fromNamespaceAndPath("mymod", "fancy_chair"),
 *     new SmartObjectDefinition(
 *         NeedType.SOCIAL,
 *         25f,   // satisfaction amount
 *         200,   // ticks to use
 *         state -> state.is(MyModBlocks.FANCY_CHAIR)
 *     )
 * );
 * }</pre>
 *
 * <p>Registration is not concurrent-safe; all calls must occur on the main server/client
 * setup thread before the world loads.
 */
public final class ColonySmartObjectAPI {

    /** All registered definitions, keyed by their namespaced ID. Insertion-ordered for determinism. */
    private static final Map<ResourceLocation, SmartObjectDefinition> REGISTRY = new LinkedHashMap<>();

    private ColonySmartObjectAPI() {}

    /**
     * Registers a smart-object definition.
     *
     * <p>If a definition with the same {@code id} was already registered, the new one replaces it
     * and a warning is logged.
     *
     * @param id         namespaced identifier, e.g. {@code mymod:fancy_chair}
     * @param definition the smart-object behaviour definition
     */
    public static void register(ResourceLocation id, SmartObjectDefinition definition) {
        if (REGISTRY.containsKey(id)) {
            ColonyMod.LOGGER.warn("[Colony] SmartObjectAPI: overwriting existing definition for '{}'", id);
        }
        REGISTRY.put(id, definition);
        ColonyMod.LOGGER.info("[Colony] SmartObjectAPI: registered '{}' (need={}, satisfaction={})",
                id, definition.targetNeed(), definition.satisfactionAmount());
    }

    /**
     * Returns an unmodifiable view of all currently registered definitions.
     *
     * @return all definitions
     */
    public static Collection<SmartObjectDefinition> getAll() {
        return Collections.unmodifiableCollection(REGISTRY.values());
    }

    /**
     * Returns the definition for the given id, or {@code null} if not registered.
     *
     * @param id the namespaced id
     * @return the definition, or {@code null}
     */
    public static SmartObjectDefinition get(ResourceLocation id) {
        return REGISTRY.get(id);
    }

    /** Returns the number of registered definitions. */
    public static int size() {
        return REGISTRY.size();
    }
}
