package com.colony.mod.entity.needs;

/**
 * Enumeration of every continuous internal stat a {@link com.colony.mod.entity.ColonistEntity}
 * tracks. Each need decays at its own rate over time and is satisfied by specific in-world actions.
 *
 * <p>Decay rates are expressed as points-per-game-tick (20 ticks = 1 second).
 */
public enum NeedType {

    /**
     * How well-fed the colonist is.
     * Decays steadily; satisfied by eating food items or cooked meals from a campfire/oven.
     */
    HUNGER(0.005f, 20f, "Hunger"),

    /**
     * Physical tiredness.
     * Decays during activity; replenished by sleeping in a bed during night hours.
     */
    ENERGY(0.004f, 20f, "Energy"),

    /**
     * Need for company and interaction.
     * Decays slowly; satisfied by talking to other colonists or using social furniture.
     */
    SOCIAL(0.002f, 20f, "Social"),

    /**
     * Sense of personal safety and shelter.
     * Drops during hostile-mob proximity or when the colonist is homeless.
     */
    SAFETY(0.003f, 20f, "Safety");

    /** How many need-points are lost per game tick under normal conditions. */
    private final float decayPerTick;

    /** The maximum value this need can reach (always 100). */
    private final float maxValue;

    /** Human-readable display name. */
    private final String displayName;

    NeedType(float decayPerTick, float maxValue, String displayName) {
        this.decayPerTick = decayPerTick;
        this.maxValue = maxValue;
        this.displayName = displayName;
    }

    public float getDecayPerTick() {
        return decayPerTick;
    }

    public float getMaxValue() {
        return maxValue;
    }

    public String getDisplayName() {
        return displayName;
    }
}
