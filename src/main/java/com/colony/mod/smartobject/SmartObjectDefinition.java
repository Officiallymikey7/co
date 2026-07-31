package com.colony.mod.smartobject;

import com.colony.mod.entity.needs.NeedType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Predicate;

/**
 * Immutable definition of a smart-object type, usable by both the Colony mod itself and
 * third-party mods via the {@link ColonySmartObjectAPI}.
 *
 * <p>A definition declares:
 * <ul>
 *   <li>Which {@link NeedType} this object satisfies</li>
 *   <li>How many need-points it restores per use</li>
 *   <li>How many ticks of interaction are required to deliver the full benefit</li>
 *   <li>A {@link BlockState} predicate that identifies which in-world blocks qualify</li>
 * </ul>
 *
 * <p>Example registration from a third-party mod:
 * <pre>{@code
 * ColonySmartObjectAPI.register(
 *     ResourceLocation.fromNamespaceAndPath("mymod", "fancy_chair"),
 *     new SmartObjectDefinition(NeedType.SOCIAL, 20f, 200,
 *         state -> state.is(MyModBlocks.FANCY_CHAIR.get()))
 * );
 * }</pre>
 */
public record SmartObjectDefinition(
        NeedType targetNeed,
        float satisfactionAmount,
        int usageDurationTicks,
        Predicate<BlockState> blockMatcher
) {
    /**
     * Returns {@code true} if the given block state represents this type of smart object.
     *
     * @param state the in-world block state to test
     * @return whether this definition matches the block
     */
    public boolean matches(BlockState state) {
        return blockMatcher.test(state);
    }
}
