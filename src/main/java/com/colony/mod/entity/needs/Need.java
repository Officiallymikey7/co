package com.colony.mod.entity.needs;

/**
 * Represents a single continuous need stat for a colonist (range 0–100).
 *
 * <p>Needs decay automatically each game tick. They can be satisfied (increased) when the
 * colonist performs the appropriate action. The current value is used by the
 * {@link com.colony.mod.entity.ai.UtilityAI} to compute urgency scores.
 */
public class Need {

    private static final float MIN_VALUE = 0f;

    private final NeedType type;
    private float value;

    /**
     * Creates a need initialised to its maximum value (full satisfaction).
     *
     * @param type the category of this need
     */
    public Need(NeedType type) {
        this.type = type;
        this.value = type.getMaxValue();
    }

    // -------------------------------------------------------------------------
    // Tick update
    // -------------------------------------------------------------------------

    /**
     * Applies one game-tick of natural decay to this need.
     * The decay rate is scaled by {@link com.colony.mod.ColonyConfig#getNeedDecayMultiplier()}
     * so server admins can tune it via config without touching code.
     * The value is clamped to {@code [0, maxValue]}.
     */
    public void tick() {
        float multiplier = com.colony.mod.ColonyConfig.getNeedDecayMultiplier();
        value = Math.max(MIN_VALUE, value - type.getDecayPerTick() * multiplier);
    }

    // -------------------------------------------------------------------------
    // Satisfy / deplete
    // -------------------------------------------------------------------------

    /**
     * Increases this need by {@code amount}, clamped to {@code maxValue}.
     *
     * @param amount positive number of points to restore
     */
    public void satisfy(float amount) {
        if (amount < 0) throw new IllegalArgumentException("satisfy amount must be non-negative");
        value = Math.min(type.getMaxValue(), value + amount);
    }

    /**
     * Decreases this need by {@code amount} (e.g., triggered by a hostile event),
     * clamped to 0.
     *
     * @param amount positive number of points to remove
     */
    public void deplete(float amount) {
        if (amount < 0) throw new IllegalArgumentException("deplete amount must be non-negative");
        value = Math.max(MIN_VALUE, value - amount);
    }

    // -------------------------------------------------------------------------
    // Utility scoring helpers
    // -------------------------------------------------------------------------

    /**
     * Returns the current deficit as a fraction in {@code [0, 1]}.
     * A value of {@code 1.0} means the need is completely empty; {@code 0.0} means fully satisfied.
     */
    public float deficitFraction() {
        return 1f - (value / type.getMaxValue());
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public NeedType getType() { return type; }

    public float getValue() { return value; }

    /** Sets the value directly (used when loading from saved data). */
    public void setValue(float value) {
        this.value = Math.max(MIN_VALUE, Math.min(type.getMaxValue(), value));
    }

    @Override
    public String toString() {
        return String.format("Need[%s=%.1f/%.1f]", type.getDisplayName(), value, type.getMaxValue());
    }
}
