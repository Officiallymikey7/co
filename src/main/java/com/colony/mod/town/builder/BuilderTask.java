package com.colony.mod.town.builder;

import net.minecraft.core.BlockPos;

import java.util.UUID;

/**
 * Represents a single autonomous construction task created by the
 * {@link com.colony.mod.town.ColonyStateMonitor}.
 *
 * <p>Lifecycle:
 * <ol>
 *   <li>{@code PENDING} — task has been created; waiting for a builder colonist to claim it</li>
 *   <li>{@code IN_PROGRESS} — a builder has claimed the task and is actively building</li>
 *   <li>{@code COMPLETE} — all blocks placed; the town planner applies the capacity bonus</li>
 *   <li>{@code FAILED} — builder died or ran out of materials; monitor will requeue</li>
 * </ol>
 */
public class BuilderTask {

    public enum Status { PENDING, IN_PROGRESS, COMPLETE, FAILED }

    private final StructureBlueprintType blueprint;
    private final BlockPos buildSite;

    private Status status = Status.PENDING;

    /** UUID of the colonist builder currently executing this task (null when PENDING). */
    private UUID assignedBuilder;

    /** How many blocks have been placed so far (used for progress reporting). */
    private int blocksPlaced = 0;

    public BuilderTask(StructureBlueprintType blueprint, BlockPos buildSite) {
        this.blueprint = blueprint;
        this.buildSite = buildSite.immutable();
    }

    // -------------------------------------------------------------------------
    // Assignment
    // -------------------------------------------------------------------------

    /**
     * Claims this task for the given builder colonist.
     *
     * @param builderId UUID of the colonist taking ownership of the task
     */
    public void claim(UUID builderId) {
        this.assignedBuilder = builderId;
        this.status = Status.IN_PROGRESS;
    }

    // -------------------------------------------------------------------------
    // Progress
    // -------------------------------------------------------------------------

    /** Increments the placed-blocks counter by one. */
    public void recordBlockPlaced() {
        blocksPlaced++;
    }

    /** Marks this task as successfully completed. */
    public void markComplete() {
        status = Status.COMPLETE;
    }

    /** Marks this task as failed so the monitor can requeue it. */
    public void markFailed() {
        status = Status.FAILED;
        assignedBuilder = null;
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public StructureBlueprintType getBlueprint() { return blueprint; }
    public BlockPos getBuildSite() { return buildSite; }
    public Status getStatus() { return status; }
    public UUID getAssignedBuilder() { return assignedBuilder; }
    public int getBlocksPlaced() { return blocksPlaced; }

    public boolean isPending() { return status == Status.PENDING; }
    public boolean isInProgress() { return status == Status.IN_PROGRESS; }
    public boolean isComplete() { return status == Status.COMPLETE; }
    public boolean isFailed() { return status == Status.FAILED; }

    @Override
    public String toString() {
        return String.format("BuilderTask[%s @ %s, status=%s, blocks=%d]",
                blueprint.name(), buildSite, status, blocksPlaced);
    }
}
