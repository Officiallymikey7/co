package com.colony.mod.entity.ai;

import com.colony.mod.entity.ColonistEntity;
import net.minecraft.world.level.Level;

/**
 * Lightweight context object passed to {@link UtilityAction} callbacks so that actions can
 * reference the owning entity and world without holding a hard reference themselves.
 */
public record ActionContext(ColonistEntity colonist, Level level) {

    /**
     * Convenience accessor for the colonist's current needs.
     */
    public com.colony.mod.entity.needs.NeedsComponent needs() {
        return colonist.getNeeds();
    }
}
