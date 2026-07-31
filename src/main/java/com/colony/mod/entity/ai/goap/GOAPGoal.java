package com.colony.mod.entity.ai.goap;

import java.util.Map;
import java.util.HashMap;

/**
 * Represents a high-level goal in the GOAP system — a desired world-state the colonist wants
 * to achieve.
 *
 * <p>Goals are selected by the {@link com.colony.mod.entity.ai.UtilityAI} based on current need
 * urgency, then handed to the {@link GOAPPlanner} which finds the cheapest sequence of
 * {@link GOAPAction}s that satisfies the goal's desired state.
 */
public abstract class GOAPGoal {

    private final String name;

    /** The world-state key/value pairs that must be true for this goal to be considered reached. */
    protected final Map<String, Object> desiredState = new HashMap<>();

    protected GOAPGoal(String name) {
        this.name = name;
    }

    // -------------------------------------------------------------------------
    // Goal priority
    // -------------------------------------------------------------------------

    /**
     * Returns the current priority of this goal (0–100).
     * The Utility AI calls this each cycle to determine which goal to pursue.
     *
     * @param context the current execution context
     * @return priority score in [0, 100]
     */
    public abstract float getPriority(com.colony.mod.entity.ai.ActionContext context);

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public String getName() { return name; }

    public Map<String, Object> getDesiredState() { return desiredState; }

    @Override
    public String toString() { return "GOAPGoal[" + name + "]"; }
}
