package com.colony.mod.entity.ai.goals;

import com.colony.mod.entity.ai.ActionContext;
import com.colony.mod.entity.ai.UtilityAI;
import com.colony.mod.entity.ai.goap.GOAPGoal;
import com.colony.mod.entity.needs.NeedType;

/**
 * GOAP goal: the colonist wants social interaction and fun.
 *
 * <p>Desired world state: {@code {"colonist_socialised": true}}.
 *
 * <p>The planner will backward-chain to actions such as:
 * <ol>
 *   <li>ChatWithColonistAction</li>
 *   <li>UseJukeboxAction</li>
 *   <li>SitAtBarChairAction</li>
 * </ol>
 */
public class SocializeGoal extends GOAPGoal {

    public SocializeGoal() {
        super("Socialize");
        desiredState.put("colonist_socialised", true);
    }

    @Override
    public float getPriority(ActionContext context) {
        // Weight = 0.8 — social is lower urgency than survival needs
        return UtilityAI.score(context.needs(), NeedType.SOCIAL, 0.8f);
    }
}
