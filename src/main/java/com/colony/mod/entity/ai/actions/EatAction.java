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
 * GOAP action: the colonist walks to the nearest food source and eats, restoring Hunger.
 *
 * <p>Preconditions: {@code food_available = true}<br>
 * Effects: {@code colonist_fed = true}
 */
public class EatAction extends GOAPAction {

    private static final double SEARCH_RADIUS_SQ = 64.0 * 64.0;
    private static final double USE_DISTANCE_SQ = 4.0;

    private SmartObject targetFood;
    private int ticksEating;

    public EatAction() {
        super("EatAction", 1.0f);
        preconditions.put("food_available", true);
        effects.put("colonist_fed", true);
    }

    @Override
    public boolean checkProceduralPrecondition(ActionContext ctx) {
        if (!(ctx.level() instanceof ServerLevel serverLevel)) return false;
        TownManager manager = TownManager.get(serverLevel);
        if (manager == null) return false;
        SmartObjectRegistry registry = manager.getTownData().getSmartObjectRegistry();
        BlockPos origin = ctx.colonist().blockPosition();
        targetFood = registry.findNearest(NeedType.HUNGER, origin, SEARCH_RADIUS_SQ, true);
        return targetFood != null;
    }

    @Override
    public boolean perform(ActionContext ctx) {
        if (targetFood == null) return true;

        BlockPos foodPos = targetFood.getPos();
        double distSq = ctx.colonist().blockPosition().distSqr(foodPos);

        if (distSq > USE_DISTANCE_SQ) {
            ctx.colonist().getNavigation().moveTo(
                    foodPos.getX() + 0.5, foodPos.getY(), foodPos.getZ() + 0.5, 1.0);
            return false;
        }

        ctx.colonist().getNavigation().stop();
        ticksEating++;
        if (ticksEating >= targetFood.getUsageDurationTicks()) {
            ctx.colonist().getNeeds().satisfy(NeedType.HUNGER, targetFood.getSatisfactionAmount());
            targetFood.release();
            return true;
        }
        return false;
    }

    @Override
    public void reset(ActionContext ctx) {
        if (targetFood != null) {
            targetFood.release();
            targetFood = null;
        }
        ticksEating = 0;
    }
}
