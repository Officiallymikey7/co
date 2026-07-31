package com.colony.mod.entity.ai.goals;

import com.colony.mod.entity.ai.ActionContext;
import com.colony.mod.entity.ai.goap.GOAPGoal;
import com.colony.mod.entity.needs.NeedType;

/**
 * GOAP goal: the colonist wants to sleep and restore their Energy need.
 *
 * <p>Priority rises steeply when Energy drops below ~50%, using the same logistic curve
 * as the {@link com.colony.mod.entity.ai.UtilityAI}.
 *
 * <p>Desired world state: {@code {"colonist_sleeping": true}}.
 */
public class SleepGoal extends GOAPGoal {

    public SleepGoal() {
        super("Sleep");
        desiredState.put("colonist_sleeping", true);
    }

    @Override
    public float getPriority(ActionContext context) {
        float deficit = context.needs().deficitFraction(NeedType.ENERGY);
        // Weight = 1.2 — sleep is slightly more urgent than baseline when energy is critical
        return com.colony.mod.entity.ai.UtilityAI.score(context.needs(), NeedType.ENERGY, 1.2f);
    }
}
