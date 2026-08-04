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
 * GOAP action: the colonist walks to the nearest social object (jukebox, seat, notice board)
 * and socialises, restoring Social need.
 *
 * <p>Preconditions: none<br>
 * Effects: {@code colonist_socialised = true}
 */
public class SocializeAction extends GOAPAction {

    private static final double SEARCH_RADIUS_SQ = 64.0 * 64.0;
    private static final double USE_DISTANCE_SQ = 4.0;

    private SmartObject targetSocial;
    private int ticksSocialising;

    public SocializeAction() {
        super("SocializeAction", 1.0f);
        // No preconditions — colonist can always attempt to socialise
        effects.put("colonist_socialised", true);
    }

    @Override
    public boolean checkProceduralPrecondition(ActionContext ctx) {
        if (!(ctx.level() instanceof ServerLevel serverLevel)) return false;
        TownManager manager = TownManager.get(serverLevel);
        if (manager == null) return false;
        SmartObjectRegistry registry = manager.getTownData().getSmartObjectRegistry();
        BlockPos origin = ctx.colonist().blockPosition();
        targetSocial = registry.findNearest(NeedType.SOCIAL, origin, SEARCH_RADIUS_SQ, true);
        return targetSocial != null;
    }

    @Override
    public boolean perform(ActionContext ctx) {
        if (targetSocial == null) return true;

        BlockPos socialPos = targetSocial.getPos();
        double distSq = ctx.colonist().blockPosition().distSqr(socialPos);

        if (distSq > USE_DISTANCE_SQ) {
            ctx.colonist().getNavigation().moveTo(
                    socialPos.getX() + 0.5, socialPos.getY(), socialPos.getZ() + 0.5, 1.0);
            return false;
        }

        ctx.colonist().getNavigation().stop();
        ticksSocialising++;
        if (ticksSocialising >= targetSocial.getUsageDurationTicks()) {
            ctx.colonist().getNeeds().satisfy(NeedType.SOCIAL, targetSocial.getSatisfactionAmount());
            targetSocial.release();
            return true;
        }
        return false;
    }

    @Override
    public void reset(ActionContext ctx) {
        if (targetSocial != null) {
            targetSocial.release();
            targetSocial = null;
        }
        ticksSocialising = 0;
    }
}
