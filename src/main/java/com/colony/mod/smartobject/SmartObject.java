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
 * <p>A {@code SmartObject} can be backed by either a legacy {@link SmartObjectType} enum value
 * (for built-in colony blocks) or a dynamic {@link SmartObjectDefinition} (for third-party mod
 * blocks registered via {@link ColonySmartObjectAPI}).
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

    private final SmartObjectType type;       // null when definition is used
    private final SmartObjectDefinition definition; // null when type is used
    private final BlockPos pos;
    private boolean reserved;

    /** Constructs a legacy-backed smart object. */
    public SmartObject(SmartObjectType type, BlockPos pos) {
        this.type = type;
        this.definition = null;
        this.pos = pos;
        this.reserved = false;
    }

    /** Constructs a definition-backed smart object (supports third-party mods). */
    public SmartObject(SmartObjectDefinition definition, BlockPos pos) {
        this.type = null;
        this.definition = definition;
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
        return type != null ? type.getTargetNeed() : definition.targetNeed();
    }

    /**
     * Returns the amount of need satisfaction this object provides when used.
     *
     * @return satisfaction amount in need-points
     */
    public float getSatisfactionAmount() {
        return type != null ? type.getSatisfactionAmount() : definition.satisfactionAmount();
    }

    /**
     * Returns how many ticks this object takes to deliver its full benefit.
     *
     * @return usage duration in ticks
     */
    public int getUsageDurationTicks() {
        return type != null ? type.getUsageDurationTicks() : definition.usageDurationTicks();
    }

    // -------------------------------------------------------------------------
    // Reservation
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
    public SmartObjectDefinition getDefinition() { return definition; }
    public BlockPos getPos() { return pos; }

    @Override
    public String toString() {
        String typeName = type != null ? type.name() : (definition != null ? definition.targetNeed().name() + "-def" : "?");
        return String.format("SmartObject[%s @ %s, reserved=%b]", typeName, pos, reserved);
    }
}
