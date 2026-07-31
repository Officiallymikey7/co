package com.colony.mod.town;

import net.minecraft.server.level.ServerLevel;

/**
 * Server-side manager that owns the {@link TownData} and drives the
 * {@link ColonyStateMonitor} on every server tick.
 *
 * <p>One {@code TownManager} instance lives per {@link ServerLevel}. It is attached
 * via NeoForge's level capability / saved-data system so it persists with the world.
 */
public class TownManager {

    private final TownData townData;
    private final ColonyStateMonitor stateMonitor;

    public TownManager() {
        this.townData = new TownData();
        this.stateMonitor = new ColonyStateMonitor();
    }

    // -------------------------------------------------------------------------
    // Tick
    // -------------------------------------------------------------------------

    /**
     * Called every server tick. Drives the Colony State Monitor evaluation loop.
     *
     * @param level the server level this manager is attached to
     */
    public void serverTick(ServerLevel level) {
        stateMonitor.tick(townData);
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public TownData getTownData() { return townData; }
    public ColonyStateMonitor getStateMonitor() { return stateMonitor; }
}
