package com.colony.mod.entity.ai.goals;

import com.colony.mod.entity.ai.ActionContext;
import com.colony.mod.entity.ai.UtilityAI;
import com.colony.mod.entity.ai.goap.GOAPGoal;
import com.colony.mod.entity.needs.NeedType;

/**
 * GOAP goal: the colonist wants to eat and restore their Hunger need.
 *
 * <p>Desired world state: {@code {"colonist_fed": true}}.
 *
 * <p>The planner will backward-chain from this goal to actions such as:
 * <ol>
 *   <li>EatMealAction (requires food_available = true)</li>
 *   <li>CookFoodAction (requires raw_ingredients = true)</li>
 *   <li>GatherIngredientsAction (requires storage_accessible = true)</li>
 * </ol>
 */
public class EatGoal extends GOAPGoal {

    public EatGoal() {
        super("Eat");
        desiredState.put("colonist_fed", true);
    }

    @Override
    public float getPriority(ActionContext context) {
        // Weight = 1.0 — normal urgency
        return UtilityAI.score(context.needs(), NeedType.HUNGER, 1.0f);
    }
}
