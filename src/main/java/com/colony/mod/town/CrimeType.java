package com.colony.mod.town;

/**
 * Types of crimes a player (or colonist) can commit against the colony.
 *
 * <p>When a crime is detected, a {@link com.colony.mod.event.CrimeCommittedEvent} is fired and
 * the perpetrator's UUID is added to the {@link LawRecord} crime blacklist. Guard colonists are
 * then dispatched via {@link com.colony.mod.entity.ai.goals.EnforceOrderGoal}.
 */
public enum CrimeType {

    /** Taking items from a colony-owned chest without permission. */
    THEFT("Theft"),

    /** Attacking a colonist. */
    ASSAULT("Assault"),

    /** Entering a restricted colony area (e.g. another colonist's home). */
    TRESPASS("Trespass");

    private final String displayName;

    CrimeType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }
}
