package com.colony.mod.smartobject;

import com.colony.mod.entity.needs.NeedType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side registry of all {@link SmartObject}s currently active in the world.
 *
 * <p>Blocks that act as smart objects (beds, campfires, jukeboxes, etc.) register themselves
 * here when placed and unregister when broken. Colonists query this registry to find nearby
 * objects that can satisfy a given need.
 *
 * <p>In addition to explicit {@link #register} calls, the registry supports chunk scanning
 * via {@link #scanChunk}: every block position in the given chunk is tested against all
 * registered {@link SmartObjectDefinition}s (both built-in via {@link SmartObjectType} and
 * third-party via {@link ColonySmartObjectAPI}) and matching blocks are auto-registered.
 *
 * <p>One registry instance exists per dimension. It is owned by the
 * {@link com.colony.mod.town.TownData} for that level and serialised with world data.
 */
public class SmartObjectRegistry {

    /** All registered smart objects, keyed by block position for O(1) lookup. */
    private final Map<BlockPos, SmartObject> objects = new ConcurrentHashMap<>();

    // -------------------------------------------------------------------------
    // Registration
    // -------------------------------------------------------------------------

    /**
     * Registers a new smart object at the given position using a legacy {@link SmartObjectType}.
     * If an object is already registered at that position it is replaced.
     *
     * @param type the smart-object type to register
     * @param pos  the block position
     */
    public void register(SmartObjectType type, BlockPos pos) {
        objects.put(pos.immutable(), new SmartObject(type, pos.immutable()));
    }

    /**
     * Registers a new smart object at the given position using an extensible
     * {@link SmartObjectDefinition}. This is the preferred method when integrating
     * third-party blocks via {@link ColonySmartObjectAPI}.
     *
     * @param definition the smart-object definition
     * @param pos        the block position
     */
    public void registerDynamic(SmartObjectDefinition definition, BlockPos pos) {
        objects.put(pos.immutable(), new SmartObject(definition, pos.immutable()));
    }

    /**
     * Removes the smart object at the given position (e.g., when the block is broken).
     *
     * @param pos the block position
     */
    public void unregister(BlockPos pos) {
        objects.remove(pos);
    }

    // -------------------------------------------------------------------------
    // Chunk scanning
    // -------------------------------------------------------------------------

    /**
     * Scans a 16×16 region centred on {@code chunkOrigin} and registers any block that matches
     * a known {@link SmartObjectDefinition} (from built-in types or third-party mods via
     * {@link ColonySmartObjectAPI}).
     *
     * <p>This should be called when a colony chunk is loaded for the first time or after a
     * world restart, so blocks placed while the server was offline are picked up.
     *
     * @param level       the world to scan
     * @param chunkOrigin the south-west corner of the chunk (y is ignored; scans y 0–255)
     */
    public void scanChunk(Level level, BlockPos chunkOrigin) {
        Collection<SmartObjectDefinition> allDefs = buildAllDefinitions();
        if (allDefs.isEmpty()) return;

        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                for (int y = level.getMinBuildHeight(); y < level.getMaxBuildHeight(); y++) {
                    BlockPos pos = chunkOrigin.offset(dx, y - chunkOrigin.getY(), dz);
                    BlockState state = level.getBlockState(pos);
                    if (state.isAir()) continue;

                    for (SmartObjectDefinition def : allDefs) {
                        if (def.matches(state)) {
                            registerDynamic(def, pos);
                            break; // first matching definition wins
                        }
                    }
                }
            }
        }
    }

    /**
     * Builds the list of definitions currently exposed by {@link ColonySmartObjectAPI}.
     *
     * <p>Built-in legacy types are expected to be seeded into that API during mod setup.
     */
    private Collection<SmartObjectDefinition> buildAllDefinitions() {
        List<SmartObjectDefinition> all = new ArrayList<>(ColonySmartObjectAPI.getAll());
        return all;
    }

    // -------------------------------------------------------------------------
    // Query
    // -------------------------------------------------------------------------

    /**
     * Returns all unreserved smart objects that satisfy the given need, within
     * {@code maxDistanceSq} squared blocks of {@code origin}.
     *
     * <p>Results are sorted nearest-first so the colonist prefers the closest option.
     *
     * @param targetNeed    the need to satisfy
     * @param origin        the colonist's current position
     * @param maxDistanceSq maximum squared distance to search
     * @return list of candidate smart objects, sorted nearest-first
     */
    public List<SmartObject> findNearest(NeedType targetNeed, BlockPos origin, double maxDistanceSq) {
        List<SmartObject> candidates = new ArrayList<>();
        for (SmartObject obj : objects.values()) {
            if (obj.getTargetNeed() != targetNeed) continue;
            if (obj.isReserved()) continue;
            double distSq = obj.getPos().distSqr(origin);
            if (distSq <= maxDistanceSq) {
                candidates.add(obj);
            }
        }
        candidates.sort(Comparator.comparingDouble(o -> o.getPos().distSqr(origin)));
        return candidates;
    }

    /**
     * Returns the nearest unreserved smart object of any type that satisfies the given need,
     * or {@code null} if none is within range.
     */
    public SmartObject findNearest(NeedType targetNeed, BlockPos origin, double maxDistanceSq,
                                   @SuppressWarnings("unused") boolean single) {
        return findNearest(targetNeed, origin, maxDistanceSq).stream().findFirst().orElse(null);
    }

    /**
     * Returns the {@link SmartObject} at the given position, or {@code null} if not registered.
     */
    public SmartObject getAt(BlockPos pos) {
        return objects.get(pos);
    }

    /** Returns an unmodifiable view of all registered smart objects. */
    public Collection<SmartObject> getAll() {
        return Collections.unmodifiableCollection(objects.values());
    }

    /** Returns the total number of registered smart objects. */
    public int size() {
        return objects.size();
    }
}
