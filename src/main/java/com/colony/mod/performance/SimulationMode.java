package com.colony.mod.performance;

/**
 * Controls how a colony is simulated based on whether its chunks are currently loaded.
 *
 * <ul>
 *   <li>{@link #FULL_3D} — full GOAP/pathfinding loop, physical block interactions, all systems active.</li>
 *   <li>{@link #ABSTRACT} — chunk unloaded; statistical math loops replace individual AI. No pathfinding,
 *       no block placement. Resources tick via aggregate rates.</li>
 * </ul>
 */
public enum SimulationMode {

    /**
     * Full 3-D simulation: all colonist entities are loaded and receiving individual
     * GOAP/pathfinding ticks. This is the normal mode when the town centre chunk is loaded.
     */
    FULL_3D,

    /**
     * Abstract (tickless) simulation: the town centre chunk is unloaded. Individual entity AI
     * is suspended; resource levels evolve through statistical deltas computed at the
     * {@link com.colony.mod.ColonyConfig#getAbstractSimTickInterval()} interval.
     */
    ABSTRACT
}
