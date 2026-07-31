package com.colony.mod.entity.schedule;

/**
 * The phases of a colonist's daily routine.
 *
 * <p>Colonists cycle through these phases each in-game day. The
 * {@link DailySchedule} determines which phase applies at any given game-time tick.
 */
public enum SchedulePhase {

    /**
     * 05:00 – 07:00 (in-game time 1000–2600 ticks).
     * Colonist wakes, gets out of bed, and eats breakfast.
     */
    WAKE_UP,

    /**
     * 07:00 – 12:00 (ticks 2600–6000).
     * Colonist performs their primary job (farming, building, crafting, etc.).
     */
    MORNING_WORK,

    /**
     * 12:00 – 13:00 (ticks 6000–6600).
     * Colonist eats a midday meal and relaxes briefly.
     */
    LUNCH,

    /**
     * 13:00 – 18:00 (ticks 6600–9000).
     * Second work block — same job or secondary tasks.
     */
    AFTERNOON_WORK,

    /**
     * 18:00 – 21:00 (ticks 9000–13000).
     * Colonist visits the town square, chats with others, uses social furniture.
     */
    FREE_TIME,

    /**
     * 21:00 – 05:00 (ticks 13000–1000 next day).
     * Colonist sleeps in their assigned bed.
     */
    SLEEP
}
