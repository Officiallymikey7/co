package com.colony.mod.entity.ai;

import com.colony.mod.entity.needs.NeedsComponent;
import com.colony.mod.entity.needs.NeedType;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Utility AI engine for colonist decision-making.
 *
 * <p>Rather than strict if/else logic, every candidate action scores its own urgency dynamically
 * each evaluation cycle. The formula is:
 *
 * <pre>
 *   utilityScore = f(deficitFraction) × weight
 * </pre>
 *
 * where {@code f} is a logistic (S-curve) function that amplifies urgency as the deficit
 * approaches 1. This ensures needs that are nearly empty dominate the decision.
 *
 * <p>Example: if Energy is at 10% (deficit = 0.90) and Hunger is at 60% (deficit = 0.40), the
 * Sleep action will score ~95/100 while the Eat action scores ~48/100, so Sleep wins.
 */
public final class UtilityAI {

    /**
     * Steepness of the logistic curve. Higher values make the urgency jump happen faster
     * as the need empties.
     */
    private static final float LOGISTIC_STEEPNESS = 8f;

    /** Midpoint of the logistic curve (deficit at which urgency = 0.5). */
    private static final float LOGISTIC_MIDPOINT = 0.5f;

    private UtilityAI() {}

    // -------------------------------------------------------------------------
    // Scoring
    // -------------------------------------------------------------------------

    /**
     * Computes the utility score (0–100) for an action that targets the given need type.
     *
     * <p>The score rises steeply as {@code deficitFraction} approaches 1. A deficit of 0
     * (fully satisfied) returns approximately 0, a deficit of 0.5 returns approximately 50,
     * and a deficit of 0.9 returns approximately 95.
     *
     * @param needs  the colonist's current needs component
     * @param target the need this action would satisfy
     * @param weight priority multiplier for this action (1.0 = normal priority)
     * @return utility score in [0, 100]
     */
    public static float score(NeedsComponent needs, NeedType target, float weight) {
        float deficit = needs.deficitFraction(target);
        float logistic = logistic(deficit);
        return Math.min(100f, logistic * 100f * weight);
    }

    /**
     * Evaluates all candidate {@link UtilityAction}s against the colonist's current needs and
     * returns them sorted from highest to lowest score.
     *
     * @param needs   the colonist's needs component
     * @param actions the list of actions available this cycle
     * @return a new list of actions sorted by descending score
     */
    public static List<ScoredAction> evaluate(NeedsComponent needs, List<UtilityAction> actions) {
        return actions.stream()
                .map(action -> new ScoredAction(action, action.score(needs)))
                .sorted(Comparator.comparingDouble(ScoredAction::score).reversed())
                .toList();
    }

    /**
     * Returns the highest-scoring action for the colonist's current needs.
     *
     * @param needs   the colonist's needs component
     * @param actions the list of actions available this cycle
     * @return the best action, or {@code null} if the list is empty
     */
    public static UtilityAction selectBest(NeedsComponent needs, List<UtilityAction> actions) {
        return actions.stream()
                .max(Comparator.comparingDouble(a -> a.score(needs)))
                .orElse(null);
    }

    // -------------------------------------------------------------------------
    // Math helpers
    // -------------------------------------------------------------------------

    /**
     * Logistic (sigmoid) function centred at {@link #LOGISTIC_MIDPOINT}.
     * Returns values in {@code (0, 1)}.
     *
     * @param x input in [0, 1]
     * @return sigmoid output
     */
    static float logistic(float x) {
        return 1f / (1f + (float) Math.exp(-LOGISTIC_STEEPNESS * (x - LOGISTIC_MIDPOINT)));
    }

    // -------------------------------------------------------------------------
    // Inner types
    // -------------------------------------------------------------------------

    /**
     * An action paired with its computed utility score for a specific evaluation cycle.
     *
     * @param action the candidate action
     * @param score  computed utility score in [0, 100]
     */
    public record ScoredAction(UtilityAction action, float score) {}
}
