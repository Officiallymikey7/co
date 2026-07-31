package com.colony.mod.entity.ai.goap;

import java.util.Map;
import java.util.HashMap;

/**
 * Represents a single atomic action in the GOAP (Goal-Oriented Action Planning) system.
 *
 * <p>Actions have:
 * <ul>
 *   <li><b>Preconditions</b> — world-state key/value pairs that must be true before the action
 *       can run.</li>
 *   <li><b>Effects</b> — world-state key/value pairs that become true after the action
 *       completes.</li>
 *   <li><b>Cost</b> — a relative execution cost used by the planner to prefer cheaper plans.</li>
 * </ul>
 *
 * <p>The GOAP planner performs backward chaining: starting from the goal state, it selects
 * actions whose effects satisfy unsatisfied state requirements, continuing until all
 * preconditions are met by the current world state.
 */
public abstract class GOAPAction {

    private final String name;
    private final float cost;

    /** State requirements that must be satisfied before this action can run. */
    protected final Map<String, Object> preconditions = new HashMap<>();

    /** State changes this action produces when it completes. */
    protected final Map<String, Object> effects = new HashMap<>();

    protected GOAPAction(String name, float cost) {
        this.name = name;
        this.cost = cost;
    }

    // -------------------------------------------------------------------------
    // Plan-time interface (used by GOAPPlanner)
    // -------------------------------------------------------------------------

    public Map<String, Object> getPreconditions() { return preconditions; }
    public Map<String, Object> getEffects() { return effects; }
    public float getCost() { return cost; }
    public String getName() { return name; }

    // -------------------------------------------------------------------------
    // Run-time interface (used during execution)
    // -------------------------------------------------------------------------

    /**
     * Returns {@code true} if real-world conditions allow this action to start.
     * This is checked at runtime, not at plan time.
     *
     * @param context the current execution context
     * @return whether the action is currently possible
     */
    public abstract boolean checkProceduralPrecondition(com.colony.mod.entity.ai.ActionContext context);

    /**
     * Performs one tick of this action's work.
     *
     * @param context the current execution context
     * @return {@code true} if the action has completed; {@code false} to continue next tick
     */
    public abstract boolean perform(com.colony.mod.entity.ai.ActionContext context);

    /**
     * Called when this action is aborted mid-execution. Clean up any in-progress state here.
     *
     * @param context the current execution context
     */
    public void reset(com.colony.mod.entity.ai.ActionContext context) {}

    @Override
    public String toString() {
        return "GOAPAction[" + name + ", cost=" + cost + "]";
    }
}
