package com.colony.mod.entity.ai.goals;

import com.colony.mod.entity.ai.ActionContext;
import com.colony.mod.entity.ai.goap.GOAPGoal;
import net.minecraft.world.entity.LivingEntity;

/**
 * GOAP goal for Guard colonists: pursue and detain a wanted entity.
 *
 * <p>This goal activates when the colonist's assigned colony has a non-empty crime blacklist
 * and the guard can locate the wanted entity within their patrol range. Priority is high enough
 * to override normal survival needs while the target remains visible.
 *
 * <p>Desired world state: {@code {"order_enforced": true}}.
 */
public class EnforceOrderGoal extends GOAPGoal {

    /** High priority — guards drop everything when pursuing a criminal. */
    private static final float ENFORCE_PRIORITY = 90f;

    /** The entity currently being pursued, or {@code null} if not engaged. */
    private LivingEntity currentTarget;

    public EnforceOrderGoal() {
        super("EnforceOrder");
        desiredState.put("order_enforced", true);
    }

    /**
     * Assigns the criminal target this goal should pursue.
     *
     * @param target the wanted entity
     */
    public void setTarget(LivingEntity target) {
        this.currentTarget = target;
    }

    public LivingEntity getTarget() { return currentTarget; }

    @Override
    public float getPriority(ActionContext context) {
        if (currentTarget == null || !currentTarget.isAlive()) {
            currentTarget = null;
            return 0f;
        }
        // High priority only when guard can "see" the target within follow range
        double distSq = context.colonist().distanceToSqr(currentTarget);
        double followRangeSq = 32.0 * 32.0;
        return distSq <= followRangeSq ? ENFORCE_PRIORITY : 0f;
    }

    @Override
    public String toString() {
        return "EnforceOrderGoal[target=" + (currentTarget != null ? currentTarget.getName().getString() : "none") + "]";
    }
}
