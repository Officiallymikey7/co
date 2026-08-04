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
 * GOAP action: the colonist walks to the nearest safety object (home, guard post) and shelters,
 * restoring the Safety need.
 *
 * <p>Preconditions: none<br>
 * Effects: {@code colonist_safe = true}
 */
public class SeekSafetyAction extends GOAPAction {

    private static final double SEARCH_RADIUS_SQ = 64.0 * 64.0;
    private static final double USE_DISTANCE_SQ = 4.0;

    private SmartObject targetShelter;
    private int ticksSheltering;

    public SeekSafetyAction() {
        super("SeekSafetyAction", 1.0f);
        // No preconditions — colonist always tries to reach safety
        effects.put("colonist_safe", true);
    }

    @Override
    public boolean checkProceduralPrecondition(ActionContext ctx) {
        if (!(ctx.level() instanceof ServerLevel serverLevel)) return false;
        TownManager manager = TownManager.get(serverLevel);
        if (manager == null) return false;
        SmartObjectRegistry registry = manager.getTownData().getSmartObjectRegistry();
        BlockPos origin = ctx.colonist().blockPosition();
        targetShelter = registry.findNearest(NeedType.SAFETY, origin, SEARCH_RADIUS_SQ, true);
        return targetShelter != null;
    }

    @Override
    public boolean perform(ActionContext ctx) {
        if (targetShelter == null) return true;

        BlockPos shelterPos = targetShelter.getPos();
        double distSq = ctx.colonist().blockPosition().distSqr(shelterPos);

        if (distSq > USE_DISTANCE_SQ) {
            ctx.colonist().getNavigation().moveTo(
                    shelterPos.getX() + 0.5, shelterPos.getY(), shelterPos.getZ() + 0.5, 1.0);
            return false;
        }

        ctx.colonist().getNavigation().stop();
        ticksSheltering++;
        if (ticksSheltering >= targetShelter.getUsageDurationTicks()) {
            ctx.colonist().getNeeds().satisfy(NeedType.SAFETY, targetShelter.getSatisfactionAmount());
            targetShelter.release();
            return true;
        }
        return false;
    }

    @Override
    public void reset(ActionContext ctx) {
        if (targetShelter != null) {
            targetShelter.release();
            targetShelter = null;
        }
        ticksSheltering = 0;
    }
}
