package com.colony.mod.town;

import com.colony.mod.ColonyConfig;
import com.colony.mod.town.builder.BuilderTask;
import com.colony.mod.town.builder.StructureBlueprintType;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Colony State Monitor — the macro-level brain of the autonomous colony.
 *
 * <p>This component runs a periodic server-side tick loop (every
 * {@link ColonyConfig#getColonyCheckIntervalTicks()} ticks) and checks colony metrics against
 * thresholds. When a threshold is breached, it enqueues a {@link BuilderTask} or job-assignment
 * directive so the colony self-corrects without any player input.
 *
 * <p>Also manages the NPC council tax vote cycle, triggered every
 * {@link ColonyConfig#getTaxVoteIntervalDays()} in-game days.
 *
 * <pre>
 * [ Colony State Monitor ]
 *          │
 *          ├── Population >= Housing Capacity?  ──> Build House Blueprint
 *          ├── Food Storage < Low Threshold?    ──> Build Farm
 *          └── Defence Level Low?               ──> Build Guard Post
 * </pre>
 */
public class ColonyStateMonitor {

    /** Food level below which we consider the colony at risk of starvation. */
    private static final int FOOD_LOW_THRESHOLD = 30;

    /** Defence level below which a guard post should be built. */
    private static final int DEFENCE_LOW_THRESHOLD = 1;

    private int ticksSinceLastCheck = 0;

    /** In-game days elapsed since the last tax vote. */
    private int daysSinceLastVote = 0;

    private final Random random = new Random();

    /** Pending construction tasks queued by this monitor. */
    private final List<BuilderTask> pendingTasks = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Tick entry point
    // -------------------------------------------------------------------------

    /**
     * Called every server tick by the {@link TownManager}.
     * Runs a full evaluation pass when the check interval has elapsed.
     *
     * @param townData      the live colony data to evaluate
     * @param dayJustPassed {@code true} if a new in-game day just began this tick
     */
    public void tick(TownData townData, boolean dayJustPassed) {
        ticksSinceLastCheck++;
        if (ticksSinceLastCheck >= ColonyConfig.getColonyCheckIntervalTicks()) {
            ticksSinceLastCheck = 0;
            evaluate(townData);
        }

        if (dayJustPassed) {
            daysSinceLastVote++;
            if (daysSinceLastVote >= ColonyConfig.getTaxVoteIntervalDays()) {
                daysSinceLastVote = 0;
                runTaxVoteCycle(townData);
            }
        }
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
            int max = ColonyConfig.getMaxPopulation();
            if (townData.getPopulation() >= max) return; // hard cap reached
            if (pendingTasks.size() >= ColonyConfig.getMaxConcurrentBuildTasks()) return;
            BlockPos buildSite = findBuildSite(townData.getTownCenter(), 30);
            if (buildSite != null) {
                enqueueBuild(StructureBlueprintType.SMALL_HOUSE, buildSite);
            }
        }
    }

    /**
     * If food stores are critically low, build a farm. Colonists are never force-assigned a
     * job — they remain free citizens and may choose their own role.
     */
    private void checkFood(TownData townData) {
        if (townData.getFoodStoreLevel() < FOOD_LOW_THRESHOLD) {
            if (pendingTasks.size() >= ColonyConfig.getMaxConcurrentBuildTasks()) return;
            BlockPos buildSite = findBuildSite(townData.getTownCenter(), 40);
            if (buildSite != null) {
                enqueueBuild(StructureBlueprintType.FARM, buildSite);
            }
        }
    }

    /**
     * If the colony has no defence infrastructure, build a guard post.
     */
    private void checkDefence(TownData townData) {
        if (townData.getDefenceLevel() < DEFENCE_LOW_THRESHOLD) {
            if (pendingTasks.size() >= ColonyConfig.getMaxConcurrentBuildTasks()) return;
            BlockPos buildSite = findBuildSite(townData.getTownCenter(), 25);
            if (buildSite != null) {
                enqueueBuild(StructureBlueprintType.GUARD_POST, buildSite);
            }
        }
    }

    /**
     * Automatically fills vacant job roles from the unemployed pool.
     */
    private void checkJobVacancies(TownData townData) {
        // Lightweight fallback for bootstrap situations; colonist AIs self-assign when at workstations.
    }

    // -------------------------------------------------------------------------
    // Tax vote cycle
    // -------------------------------------------------------------------------

    /**
     * Runs an NPC council vote to potentially change the colony tax rate.
     *
     * <p>Three candidate rates are generated within the config min/max range. Each
     * {@link JobRole#MERCHANT} councillor casts a vote weighted by their total affinity with
     * other colonists (a proxy for how influential they are in the community). The winning
     * rate is applied to {@link LawRecord#setTaxRate(double)}.
     */
    private void runTaxVoteCycle(TownData townData) {
        double min = ColonyConfig.getMinTaxRate();
        double max = ColonyConfig.getMaxTaxRate();
        if (min >= max) return; // degenerate range — nothing to vote on

        // Build three random proposals between min and max
        List<VoteProposal> proposals = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            double rate = min + random.nextDouble() * (max - min);
            // Round to 2 decimal places for readability
            rate = Math.round(rate * 100.0) / 100.0;
            proposals.add(new VoteProposal(rate));
        }

        townData.getLawRecord().openVote(proposals);

        List<java.util.UUID> merchants = new ArrayList<>();
        for (java.util.UUID id : townData.getColonistIds()) {
            if (townData.getJob(id) == JobRole.MERCHANT) {
                merchants.add(id);
            }
        }

        if (merchants.isEmpty()) {
            // No merchants — vote randomly
            proposals.get(random.nextInt(proposals.size())).addVote(1.0);
        } else {
            for (java.util.UUID merchantId : merchants) {
                double influence = 1.0;
                for (com.colony.mod.social.RelationshipData rel
                        : townData.getSocialNetwork().getTopRelationships(merchantId, townData.getPopulation())) {
                    influence += Math.max(0.0, rel.getAffinity());
                }

                VoteProposal choice = null;
                double bestScore = Double.NEGATIVE_INFINITY;
                for (VoteProposal p : proposals) {
                    // Merchants prefer low taxes; add mild randomness per vote.
                    double score = (1.0 / (1.0 + p.getProposedTaxRate())) + random.nextDouble() * 0.3;
                    if (score > bestScore) {
                        bestScore = score;
                        choice = p;
                    }
                }
                if (choice != null) {
                    choice.addVote(influence);
                }
            }
        }

        townData.getLawRecord().closeVote();
    }

    // -------------------------------------------------------------------------
    // Build task creation
    // -------------------------------------------------------------------------

    private void enqueueBuild(StructureBlueprintType blueprint, BlockPos site) {
        for (BuilderTask task : pendingTasks) {
            if (task.getBlueprint() == blueprint) return; // already pending
        }
        pendingTasks.add(new BuilderTask(blueprint, site));
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
     */
    private BlockPos findBuildSite(BlockPos center, int searchRadius) {
        for (int r = 10; r <= searchRadius; r += 5) {
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
