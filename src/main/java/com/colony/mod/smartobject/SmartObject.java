package com.colony.mod.smartobject;

import com.colony.mod.entity.needs.NeedType;
import net.minecraft.core.BlockPos;

/**
 * A "Smart Object" is any block or entity in the world that advertises its utility to nearby
 * colonists.
 *
 * <p>Instead of hard-coding every block interaction into colonist brain logic, blocks
 * register as smart objects. When a colonist's need drops, the colonist searches their
 * awareness radius for smart objects that advertise utility for that need and pathfinds
 * toward the best candidate.
 *
 * <p>Examples:
 * <ul>
 *   <li>A Bed advertises {@code Rest → +80 Energy}</li>
 *   <li>A Campfire/Oven advertises {@code Cooking → +50 Hunger}</li>
 *   <li>A Jukebox advertises {@code Music → +30 Social}</li>
 *   <li>A Chest advertises {@code Storage access → enables cooking}</li>
 * </ul>
 */
public class SmartObject {

    private final SmartObjectType type;
    private final BlockPos pos;
    private boolean reserved;

    public SmartObject(SmartObjectType type, BlockPos pos) {
        this.type = type;
        this.pos = pos;
        this.reserved = false;
    }

    // -------------------------------------------------------------------------
    // Utility query
    // -------------------------------------------------------------------------

    /**
     * Returns the need type this smart object satisfies.
     *
     * @return the target need type
     */
    public NeedType getTargetNeed() {
        return type.getTargetNeed();
    }

    /**
     * Returns the amount of need satisfaction this object provides when used.
     *
     * @return satisfaction amount in need-points (added to the need's current value)
     */
    public float getSatisfactionAmount() {
        return type.getSatisfactionAmount();
    }

    /**
     * Returns how many ticks this object takes to deliver its full benefit.
     *
     * @return usage duration in ticks
     */
    public int getUsageDurationTicks() {
        return type.getUsageDurationTicks();
    }

    // -------------------------------------------------------------------------
    // Reservation (prevents multiple colonists racing to the same object)
    // -------------------------------------------------------------------------

    /** Returns {@code true} if another colonist has already claimed this object. */
    public boolean isReserved() { return reserved; }

    /** Marks this object as reserved by a colonist. */
    public void reserve() { reserved = true; }

    /** Releases the reservation so other colonists can use the object. */
    public void release() { reserved = false; }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public SmartObjectType getType() { return type; }
    public BlockPos getPos() { return pos; }

    @Override
    public String toString() {
        return String.format("SmartObject[%s @ %s, reserved=%b]", type.name(), pos, reserved);
    }
}
