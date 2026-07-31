package com.colony.mod.entity.ai.goals;

import com.colony.mod.entity.ai.ActionContext;
import com.colony.mod.entity.ai.UtilityAI;
import com.colony.mod.entity.ai.goap.GOAPGoal;
import com.colony.mod.entity.needs.NeedType;

/**
 * GOAP goal: the colonist wants to feel safe and sheltered.
 *
 * <p>Priority spikes when hostile mobs are nearby or the colonist is homeless.
 *
 * <p>Desired world state: {@code {"colonist_safe": true}}.
 */
public class SeekSafetyGoal extends GOAPGoal {

    public SeekSafetyGoal() {
        super("SeekSafety");
        desiredState.put("colonist_safe", true);
    }

    @Override
    public float getPriority(ActionContext context) {
        // Weight = 1.5 — safety is the highest-priority survival need
        return UtilityAI.score(context.needs(), NeedType.SAFETY, 1.5f);
    }
}
