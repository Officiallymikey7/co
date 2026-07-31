package com.colony.mod.entity.ai;

import com.colony.mod.entity.needs.NeedsComponent;

/**
 * Contract for any action that can be evaluated by the {@link UtilityAI}.
 *
 * <p>Implementations declare:
 * <ol>
 *   <li>How urgent they are given the colonist's current needs ({@link #score})</li>
 *   <li>Whether they can currently be executed ({@link #canExecute})</li>
 *   <li>The execution logic ({@link #execute})</li>
 * </ol>
 */
public interface UtilityAction {

    /**
     * Computes this action's urgency score (0–100) given the colonist's current need state.
     * Higher scores mean the action is more important right now.
     *
     * @param needs the colonist's current needs component
     * @return urgency score in [0, 100]
     */
    float score(NeedsComponent needs);

    /**
     * Returns {@code true} if this action can start or continue being executed right now
     * (e.g., required objects are in range, conditions are met).
     *
     * @param context the colonist entity context
     * @return whether the action is currently executable
     */
    boolean canExecute(ActionContext context);

    /**
     * Performs one game-tick of this action's logic.
     *
     * @param context the colonist entity context
     */
    void execute(ActionContext context);

    /**
     * Called when this action is interrupted (e.g., a higher-scoring action took over).
     * Implementations should clean up any in-progress state here.
     *
     * @param context the colonist entity context
     */
    default void onInterrupted(ActionContext context) {}

    /**
     * Human-readable name used for debugging and logging.
     */
    String getName();
}
