package com.colony.mod.entity.ai.goals;

import com.colony.mod.entity.ai.ActionContext;
import com.colony.mod.entity.ai.goap.GOAPGoal;

/**
 * GOAP goal: the colonist wants to perform their assigned job.
 *
 * <p>Work priority is moderate — it yields to critical survival needs (hunger, sleep, safety)
 * but takes over when the colonist is reasonably well-rested and fed.
 *
 * <p>Desired world state: {@code {"colonist_worked": true}}.
 */
public class WorkGoal extends GOAPGoal {

    /** Base priority for the work goal (applied when all survival needs are above threshold). */
    private static final float BASE_WORK_PRIORITY = 45f;

    public WorkGoal() {
        super("Work");
        desiredState.put("colonist_worked", true);
    }

    @Override
    public float getPriority(ActionContext context) {
        // Work gets a flat base priority that survival goals will outbid when urgent.
        // Future enhancement: vary priority based on time-of-day schedule.
        return BASE_WORK_PRIORITY;
    }
}
