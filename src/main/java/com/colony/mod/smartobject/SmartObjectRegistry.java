package com.colony.mod.smartobject;

import com.colony.mod.entity.needs.NeedType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side registry of all {@link SmartObject}s currently active in the world.
 *
 * <p>Blocks that act as smart objects (beds, campfires, jukeboxes, etc.) register themselves
 * here when placed and unregister when broken. Colonists query this registry to find nearby
 * objects that can satisfy a given need.
 *
 * <p>One registry instance exists per {@link Level} (dimension). It is owned by the
 * {@link com.colony.mod.town.TownData} for that level and serialised with world data.
 */
public class SmartObjectRegistry {

    /** All registered smart objects, keyed by block position for O(1) lookup. */
    private final Map<BlockPos, SmartObject> objects = new ConcurrentHashMap<>();

    // -------------------------------------------------------------------------
    // Registration
    // -------------------------------------------------------------------------

    /**
     * Registers a new smart object at the given position.
     * If an object is already registered at that position it is replaced.
     *
     * @param type the smart-object type to register
     * @param pos  the block position
     */
    public void register(SmartObjectType type, BlockPos pos) {
        objects.put(pos.immutable(), new SmartObject(type, pos.immutable()));
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
    // Query
    // -------------------------------------------------------------------------

    /**
     * Returns all unreserved smart objects that satisfy the given need, within
     * {@code maxDistanceSq} squared blocks of {@code origin}.
     *
     * <p>Results are sorted nearest-first so the colonist prefers the closest option.
     *
     * @param targetNeed   the need to satisfy
     * @param origin       the colonist's current position
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
     *
     * @param targetNeed   the need to satisfy
     * @param origin       the colonist's current position
     * @param maxDistanceSq maximum squared distance to search
     * @return nearest candidate, or {@code null}
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
