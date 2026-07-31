package com.colony.mod.town;

import com.colony.mod.town.builder.BuilderTask;
import com.colony.mod.town.builder.StructureBlueprintType;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * Colony State Monitor — the macro-level brain of the autonomous colony.
 *
 * <p>This component runs a periodic server-side tick loop (every
 * {@link #CHECK_INTERVAL_TICKS} ticks) and checks colony metrics against thresholds.
 * When a threshold is breached, it enqueues a {@link BuilderTask} or job-assignment
 * directive so the colony self-corrects without any player input.
 *
 * <pre>
 * [ Colony State Monitor ]
 *          │
 *          ├── Population >= Housing Capacity?  ──> Build House Blueprint
 *          ├── Food Storage < Low Threshold?    ──> Assign Farmer / Build Farm
 *          └── Defence Level Low?               ──> Build Guard Post
 * </pre>
 */
public class ColonyStateMonitor {

    /** How often (in server ticks) to re-evaluate colony metrics. */
    private static final int CHECK_INTERVAL_TICKS = 200; // every 10 seconds

    /** Food level below which we consider the colony at risk of starvation. */
    private static final int FOOD_LOW_THRESHOLD = 30;

    /** Defence level below which a guard post should be built. */
    private static final int DEFENCE_LOW_THRESHOLD = 1;

    private int ticksSinceLastCheck = 0;

    /** Pending construction tasks queued by this monitor. */
    private final List<BuilderTask> pendingTasks = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Tick entry point
    // -------------------------------------------------------------------------

    /**
     * Called every server tick by the {@link TownManager}.
     * Runs a full evaluation pass when the check interval has elapsed.
     *
     * @param townData the live colony data to evaluate
     */
    public void tick(TownData townData) {
        ticksSinceLastCheck++;
        if (ticksSinceLastCheck < CHECK_INTERVAL_TICKS) return;
        ticksSinceLastCheck = 0;

        evaluate(townData);
    }

    // -------------------------------------------------------------------------
    // Evaluation passes
    // -------------------------------------------------------------------------

    private void evaluate(TownData townData) {
        checkHousing(townData);
        checkFood(townData);
        checkDefence(townData);
        checkJobVacancies(townData);
    }

    /**
     * If the colony is at or above housing capacity, trigger a new house build.
     */
    private void checkHousing(TownData townData) {
        if (townData.getPopulation() >= townData.getHousingCapacity()) {
            BlockPos buildSite = findBuildSite(townData.getTownCenter(), 30);
            if (buildSite != null) {
                enqueueBuild(StructureBlueprintType.SMALL_HOUSE, buildSite, townData);
            }
        }
    }

    /**
     * If food stores are critically low, assign a farmer or build a farm.
     */
    private void checkFood(TownData townData) {
        if (townData.getFoodStoreLevel() < FOOD_LOW_THRESHOLD) {
            // First try to promote an unemployed colonist to farmer
            List<java.util.UUID> unemployed = townData.getUnemployed();
            if (!unemployed.isEmpty()) {
                townData.assignJob(unemployed.get(0), JobRole.FARMER);
            } else {
                // All colonists employed — build a farm to increase output
                BlockPos buildSite = findBuildSite(townData.getTownCenter(), 40);
                if (buildSite != null) {
                    enqueueBuild(StructureBlueprintType.FARM, buildSite, townData);
                }
            }
        }
    }

    /**
     * If the colony has no defence infrastructure, build a guard post.
     */
    private void checkDefence(TownData townData) {
        if (townData.getDefenceLevel() < DEFENCE_LOW_THRESHOLD) {
            BlockPos buildSite = findBuildSite(townData.getTownCenter(), 25);
            if (buildSite != null) {
                enqueueBuild(StructureBlueprintType.GUARD_POST, buildSite, townData);
            }
        }
    }

    /**
     * Automatically fills vacant job roles from the unemployed pool.
     * (Lightweight check — full job matching is handled by the colonist's own AI.)
     */
    private void checkJobVacancies(TownData townData) {
        // Intentionally lightweight: colonist AIs self-assign jobs when near workstations.
        // This method is a fallback for bootstrap situations.
    }

    // -------------------------------------------------------------------------
    // Build task creation
    // -------------------------------------------------------------------------

    private void enqueueBuild(StructureBlueprintType blueprint, BlockPos site, TownData townData) {
        // Avoid duplicate tasks for the same blueprint type at a nearby location
        for (BuilderTask task : pendingTasks) {
            if (task.getBlueprint() == blueprint) return; // already pending
        }
        BuilderTask task = new BuilderTask(blueprint, site);
        pendingTasks.add(task);
    }

    /**
     * Returns all pending construction tasks so that builder colonists can claim them.
     */
    public List<BuilderTask> getPendingTasks() {
        return java.util.Collections.unmodifiableList(pendingTasks);
    }

    /**
     * Called by the builder colonist when it finishes a task, removing it from the queue.
     *
     * @param task the completed task
     */
    public void completeTask(BuilderTask task) {
        pendingTasks.remove(task);
    }

    // -------------------------------------------------------------------------
    // Build site selection
    // -------------------------------------------------------------------------

    /**
     * Finds a candidate build site near the town centre.
     * Returns the first position that is clear (placeholder logic; real implementation
     * should check for flat terrain and absence of existing structures).
     *
     * @param center    town centre block position
     * @param searchRadius radius to search in
     * @return a candidate build position, or {@code null} if none found
     */
    private BlockPos findBuildSite(BlockPos center, int searchRadius) {
        // Simple spiral search — real implementation would check Level for solid ground.
        for (int r = 10; r <= searchRadius; r += 5) {
            // North offset
            BlockPos candidate = center.offset(r, 0, 0);
            if (isSuitableSite(candidate)) return candidate;
            candidate = center.offset(-r, 0, 0);
            if (isSuitableSite(candidate)) return candidate;
            candidate = center.offset(0, 0, r);
            if (isSuitableSite(candidate)) return candidate;
            candidate = center.offset(0, 0, -r);
            if (isSuitableSite(candidate)) return candidate;
        }
        return null;
    }

    /**
     * Placeholder check — real implementation uses Level to verify flat terrain,
     * absence of water, and no overlapping structures.
     */
    private boolean isSuitableSite(BlockPos pos) {
        // TODO: inject Level reference and perform real terrain check
        return true;
    }
}
