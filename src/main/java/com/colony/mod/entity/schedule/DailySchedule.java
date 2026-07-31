package com.colony.mod.entity.schedule;

/**
 * Determines which {@link SchedulePhase} a colonist should be in based on the current
 * in-game day time.
 *
 * <p>Minecraft day time runs from 0 (sunrise) to 23999 ticks per day.
 * The schedule is expressed in those same ticks.
 *
 * <table>
 *   <tr><th>Phase</th>        <th>Day-time range (ticks)</th><th>Real in-game time</th></tr>
 *   <tr><td>WAKE_UP</td>      <td>0 – 999</td>               <td>06:00 – 06:50</td></tr>
 *   <tr><td>MORNING_WORK</td> <td>1000 – 5999</td>           <td>06:50 – 12:00</td></tr>
 *   <tr><td>LUNCH</td>        <td>6000 – 7199</td>           <td>12:00 – 13:00</td></tr>
 *   <tr><td>AFTERNOON_WORK</td><td>7200 – 10999</td>         <td>13:00 – 17:00</td></tr>
 *   <tr><td>FREE_TIME</td>    <td>11000 – 12999</td>         <td>17:00 – 19:00</td></tr>
 *   <tr><td>SLEEP</td>        <td>13000 – 23999</td>         <td>19:00 – 06:00</td></tr>
 * </table>
 */
public class DailySchedule {

    // Day-time tick boundaries (Minecraft day = 24 000 ticks, day starts at 0)
    private static final int WAKE_UP_START       = 0;
    private static final int MORNING_WORK_START  = 1000;
    private static final int LUNCH_START         = 6000;
    private static final int AFTERNOON_WORK_START= 7200;
    private static final int FREE_TIME_START     = 11000;
    private static final int SLEEP_START         = 13000;

    /**
     * Returns the {@link SchedulePhase} appropriate for the given Minecraft day-time tick.
     *
     * @param dayTime the current {@code Level#getDayTime() % 24000}
     * @return the active schedule phase
     */
    public SchedulePhase getPhase(long dayTime) {
        long t = dayTime % 24000L;
        if (t < MORNING_WORK_START)    return SchedulePhase.WAKE_UP;
        if (t < LUNCH_START)           return SchedulePhase.MORNING_WORK;
        if (t < AFTERNOON_WORK_START)  return SchedulePhase.LUNCH;
        if (t < FREE_TIME_START)       return SchedulePhase.AFTERNOON_WORK;
        if (t < SLEEP_START)           return SchedulePhase.FREE_TIME;
        return SchedulePhase.SLEEP;
    }

    /**
     * Convenience: returns {@code true} if the colonist should currently be sleeping.
     *
     * @param dayTime the current {@code Level#getDayTime() % 24000}
     */
    public boolean isSleepTime(long dayTime) {
        return getPhase(dayTime) == SchedulePhase.SLEEP;
    }

    /**
     * Convenience: returns {@code true} if the colonist should currently be working.
     *
     * @param dayTime the current {@code Level#getDayTime() % 24000}
     */
    public boolean isWorkTime(long dayTime) {
        SchedulePhase phase = getPhase(dayTime);
        return phase == SchedulePhase.MORNING_WORK || phase == SchedulePhase.AFTERNOON_WORK;
    }

    /**
     * Convenience: returns {@code true} if the colonist should currently be socialising.
     *
     * @param dayTime the current {@code Level#getDayTime() % 24000}
     */
    public boolean isSocialTime(long dayTime) {
        SchedulePhase phase = getPhase(dayTime);
        return phase == SchedulePhase.FREE_TIME || phase == SchedulePhase.LUNCH;
    }
}
