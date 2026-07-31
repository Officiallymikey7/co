package com.colony.mod.smartobject;

import com.colony.mod.entity.needs.NeedType;

/**
 * Enumeration of every smart-object type recognised by the Colony mod.
 *
 * <p>Each type declares:
 * <ul>
 *   <li>Which {@link NeedType} it satisfies</li>
 *   <li>How many need-points it restores per use</li>
 *   <li>How long a colonist must interact with it (in ticks) to receive the full benefit</li>
 * </ul>
 */
public enum SmartObjectType {

    /** Vanilla or colony bed — restores Energy while the colonist sleeps. */
    BED(NeedType.ENERGY, 80f, 6000),              // ~5 minutes at 20 TPS

    /** Campfire — colonists can cook raw food and restore Hunger. */
    CAMPFIRE(NeedType.HUNGER, 50f, 200),           // 10 seconds

    /** Furnace or oven — restores Hunger (cooked meals). */
    OVEN(NeedType.HUNGER, 60f, 300),               // 15 seconds

    /** Crafting table with stored ingredients — restores Hunger. */
    FOOD_PREP_TABLE(NeedType.HUNGER, 40f, 200),

    /** Jukebox — music raises Social and Fun. */
    JUKEBOX(NeedType.SOCIAL, 30f, 400),            // 20 seconds

    /** Bar chair / bench — colonists sit and chat, restoring Social. */
    SOCIAL_SEAT(NeedType.SOCIAL, 25f, 300),

    /** Town notice board — colonists gather here, strong Social boost. */
    NOTICE_BOARD(NeedType.SOCIAL, 40f, 200),

    /** Guard post / watchtower — colonists on patrol restore Safety. */
    GUARD_POST(NeedType.SAFETY, 50f, 400),

    /** Home marker — being inside a claimed home restores Safety passively. */
    HOME(NeedType.SAFETY, 20f, 100);

    private final NeedType targetNeed;
    private final float satisfactionAmount;
    private final int usageDurationTicks;

    SmartObjectType(NeedType targetNeed, float satisfactionAmount, int usageDurationTicks) {
        this.targetNeed = targetNeed;
        this.satisfactionAmount = satisfactionAmount;
        this.usageDurationTicks = usageDurationTicks;
    }

    public NeedType getTargetNeed() { return targetNeed; }
    public float getSatisfactionAmount() { return satisfactionAmount; }
    public int getUsageDurationTicks() { return usageDurationTicks; }
}
