package com.colony.mod.entity.ai.actions;

import com.colony.mod.entity.ai.ActionContext;
import com.colony.mod.entity.ai.goap.GOAPAction;
import com.colony.mod.entity.needs.NeedType;
import com.colony.mod.smartobject.SmartObject;
import com.colony.mod.smartobject.SmartObjectRegistry;
import com.colony.mod.town.TownManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/**
 * GOAP action: the colonist walks to the nearest bed and sleeps, restoring Energy.
 *
 * <p>Preconditions: {@code has_bed = true}<br>
 * Effects: {@code colonist_sleeping = true}
 */
public class SleepAction extends GOAPAction {

    /** Squared block distance within which the colonist will search for a bed. */
    private static final double SEARCH_RADIUS_SQ = 64.0 * 64.0;

    /** How close (squared) the colonist needs to be to start sleeping. */
    private static final double USE_DISTANCE_SQ = 4.0;

    private SmartObject targetBed;
    private int ticksSlept;

    public SleepAction() {
        super("SleepAction", 1.0f);
        preconditions.put("has_bed", true);
        effects.put("colonist_sleeping", true);
    }

    @Override
    public boolean checkProceduralPrecondition(ActionContext ctx) {
        if (!(ctx.level() instanceof ServerLevel serverLevel)) return false;
        TownManager manager = TownManager.get(serverLevel);
        if (manager == null) return false;
        SmartObjectRegistry registry = manager.getTownData().getSmartObjectRegistry();
        BlockPos origin = ctx.colonist().blockPosition();
        targetBed = registry.findNearest(NeedType.ENERGY, origin, SEARCH_RADIUS_SQ, true);
        return targetBed != null;
    }

    @Override
    public boolean perform(ActionContext ctx) {
        if (targetBed == null) return true; // abort — no bed found

        BlockPos bedPos = targetBed.getPos();
        double distSq = ctx.colonist().blockPosition().distSqr(bedPos);

        if (distSq > USE_DISTANCE_SQ) {
            // Navigate toward the bed
            ctx.colonist().getNavigation().moveTo(
                    bedPos.getX() + 0.5, bedPos.getY(), bedPos.getZ() + 0.5, 1.0);
            return false;
        }

        // At the bed — stop moving and sleep
        ctx.colonist().getNavigation().stop();
        ticksSlept++;
        if (ticksSlept >= targetBed.getUsageDurationTicks()) {
            ctx.colonist().getNeeds().satisfy(NeedType.ENERGY, targetBed.getSatisfactionAmount());
            targetBed.release();
            return true;
        }
        return false;
    }

    @Override
    public void reset(ActionContext ctx) {
        if (targetBed != null) {
            targetBed.release();
            targetBed = null;
        }
        ticksSlept = 0;
    }
}
