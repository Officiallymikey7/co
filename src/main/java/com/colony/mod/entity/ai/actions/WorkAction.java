package com.colony.mod.entity.ai.actions;

import com.colony.mod.entity.ai.ActionContext;
import com.colony.mod.entity.ai.goap.GOAPAction;
import com.colony.mod.town.JobRole;

/**
 * GOAP action: the colonist performs their assigned job for a work session.
 *
 * <p>For now this is a timed idle — future iterations can navigate to a workstation
 * and trigger job-specific behaviour.
 *
 * <p>Preconditions: none (all colonists can work regardless of job role)<br>
 * Effects: {@code colonist_worked = true}
 */
public class WorkAction extends GOAPAction {

    /** Duration of a work session in ticks (~30 seconds at 20 TPS). */
    private static final int WORK_DURATION_TICKS = 600;

    private int ticksWorked;

    public WorkAction() {
        super("WorkAction", 1.0f);
        // No preconditions — any colonist can attempt to work
        effects.put("colonist_worked", true);
    }

    @Override
    public boolean checkProceduralPrecondition(ActionContext ctx) {
        // Unemployed colonists skip the work action so higher-priority survival goals can run
        return ctx.colonist().getJobRole() != JobRole.UNEMPLOYED;
    }

    @Override
    public boolean perform(ActionContext ctx) {
        // Stop wandering while working
        ctx.colonist().getNavigation().stop();
        ticksWorked++;
        return ticksWorked >= WORK_DURATION_TICKS;
    }

    @Override
    public void reset(ActionContext ctx) {
        ticksWorked = 0;
    }
}
