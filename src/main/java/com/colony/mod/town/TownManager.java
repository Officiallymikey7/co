package com.colony.mod.town;

import com.colony.mod.ColonyConfig;
import com.colony.mod.ColonyMod;
import com.colony.mod.event.CrimeCommittedEvent;
import com.colony.mod.performance.SimulationMode;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Server-side manager that owns the {@link TownData} and drives the
 * {@link ColonyStateMonitor} on every server tick.
 *
 * <p>One {@code TownManager} instance lives per {@link ServerLevel}. It handles:
 * <ul>
 *   <li>Delegating to {@link ColonyStateMonitor} each tick</li>
 *   <li>Switching between {@link SimulationMode#FULL_3D} and {@link SimulationMode#ABSTRACT}
 *       based on chunk load state</li>
 *   <li>Running abstract statistical simulation when the colony is unloaded</li>
 *   <li>Payday: distributing wages to all employed colonists and players every in-game day</li>
 *   <li>Handling {@link CrimeCommittedEvent}s</li>
 * </ul>
 */
public class TownManager {

    // -------------------------------------------------------------------------
    // Per-level singleton map (weak so it doesn't prevent level GC)
    // -------------------------------------------------------------------------

    private static final Map<ServerLevel, TownManager> INSTANCES = new WeakHashMap<>();
    private static final String TOWN_DATA_NAME = ColonyMod.MOD_ID + "_town_data";

    /** Returns (or lazily creates) the TownManager for the given level. */
    public static TownManager get(ServerLevel level) {
        return INSTANCES.computeIfAbsent(level, TownManager::createForLevel);
    }

    private static TownManager createForLevel(ServerLevel level) {
        TownSavedData saved = level.getDataStorage().computeIfAbsent(TownSavedData.factory(), TOWN_DATA_NAME);
        return new TownManager(saved);
    }

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------

    private final TownData townData;
    private final ColonyStateMonitor stateMonitor;
    private final TownSavedData savedData;

    /** Current simulation mode — switches when the town-centre chunk loads/unloads. */
    private SimulationMode simulationMode = SimulationMode.FULL_3D;

    /** Tracks ticks for day-boundary detection (one day = 24 000 ticks). */
    private long totalTicks = 0;

    /** Accumulator for abstract simulation ticks. */
    private int abstractSimCounter = 0;

    // -------------------------------------------------------------------------
    // Constants (hunger/production rates for abstract sim)
    // -------------------------------------------------------------------------

    private static final float ABSTRACT_HUNGER_RATE_PER_POP   = 0.5f; // food units lost per pop per abstract tick
    private static final float ABSTRACT_INCOME_PER_WORKER     = 2.0f; // coins earned per employed colonist per abstract tick

    // -------------------------------------------------------------------------
    // Construction
    // -------------------------------------------------------------------------

    private TownManager(TownSavedData savedData) {
        this.savedData = savedData;
        this.townData = savedData.townData();
        this.stateMonitor = new ColonyStateMonitor();
    }

    // -------------------------------------------------------------------------
    // Tick
    // -------------------------------------------------------------------------

    /**
     * Called every server tick. Drives the Colony State Monitor evaluation loop and
     * handles simulation-mode switching.
     *
     * @param level the server level this manager is attached to
     */
    public void serverTick(ServerLevel level) {
        totalTicks++;
        boolean newDay = (totalTicks % 24000L == 0);

        // Determine simulation mode from chunk load state
        boolean chunkLoaded = level.isPositionEntityTicking(townData.getTownCenter());

        if (chunkLoaded && simulationMode == SimulationMode.ABSTRACT) {
            simulationMode = SimulationMode.FULL_3D;
            ColonyMod.LOGGER.debug("[Colony] Switching to FULL_3D simulation.");
        } else if (!chunkLoaded && simulationMode == SimulationMode.FULL_3D) {
            simulationMode = SimulationMode.ABSTRACT;
            ColonyMod.LOGGER.debug("[Colony] Switching to ABSTRACT simulation.");
        }

        if (simulationMode == SimulationMode.FULL_3D) {
            stateMonitor.tick(townData, newDay);
        } else {
            tickAbstractSim();
        }

        if (newDay) {
            processPayday(level);
            processRent(level);
        }

        savedData.setDirty();
    }

    // -------------------------------------------------------------------------
    // Abstract simulation
    // -------------------------------------------------------------------------

    /**
     * Applies statistical deltas to colony resources while the colony is unloaded.
     * Runs once per {@link ColonyConfig#getAbstractSimTickInterval()} ticks.
     */
    private void tickAbstractSim() {
        abstractSimCounter++;
        if (abstractSimCounter < ColonyConfig.getAbstractSimTickInterval()) return;
        abstractSimCounter = 0;

        int pop = townData.getPopulation();
        if (pop == 0) return;

        // Food decays proportionally to population
        float foodLoss = pop * ABSTRACT_HUNGER_RATE_PER_POP;
        townData.adjustFood(-(int) foodLoss);

        // Treasury grows proportionally to employed workers
        long employed = countEmployed();
        int income = (int) (employed * ABSTRACT_INCOME_PER_WORKER);
        townData.adjustTreasury(income);

        ColonyMod.LOGGER.debug("[Colony][Abstract] food -{}, treasury +{}", (int) foodLoss, income);
    }

    private long countEmployed() {
        long count = 0;
        for (JobRole role : JobRole.values()) {
            if (role != JobRole.UNEMPLOYED) {
                count += townData.countByRole(role);
            }
        }
        return count;
    }

    // -------------------------------------------------------------------------
    // Payday
    // -------------------------------------------------------------------------

    /**
     * Called once per in-game day. Distributes wages from the town treasury to all
     * employed colonists (and players) based on their role's configured wage.
     *
     * <p>Tax is deducted from wages before deposit.
     */
    private void processPayday(ServerLevel level) {
        double taxRate = townData.getLawRecord().getTaxRate();

        for (Map.Entry<UUID, JobRole> entry : new java.util.HashMap<>(iterableJobAssignments()).entrySet()) {
            JobRole role = entry.getValue();
            if (role == JobRole.UNEMPLOYED) continue;

            int grossWage = townData.getWageForRole(role);
            int tax = (int) (grossWage * taxRate);
            int netWage = grossWage - tax;

            if (townData.getTownTreasury() < netWage) break; // treasury empty

            townData.adjustTreasury(-netWage);
            townData.depositToWallet(entry.getKey(), netWage);

            ColonyMod.LOGGER.debug("[Colony] Payday: {} earns {} coins (tax {})", entry.getKey(), netWage, tax);
        }
    }

    /** Helper to get a snapshot of job assignments for iteration. */
    private java.util.Map<UUID, JobRole> iterableJobAssignments() {
        java.util.Map<UUID, JobRole> map = new java.util.HashMap<>();
        for (UUID id : townData.getColonistIds()) {
            map.put(id, townData.getJob(id));
        }
        return map;
    }

    private java.util.Set<UUID> allColonistIds() {
        return townData.getColonistIds();
    }

    // -------------------------------------------------------------------------
    // Rent collection
    // -------------------------------------------------------------------------

    /**
     * Deducts nightly rent from tenant wallets. If the tenant cannot pay, their home is
     * revoked and they become homeless.
     */
    private void processRent(ServerLevel level) {
        for (Map.Entry<net.minecraft.core.BlockPos, Integer> entry : townData.getRentPrices().entrySet()) {
            net.minecraft.core.BlockPos home = entry.getKey();
            int rent = entry.getValue();
            if (rent <= 0) continue;
            if (townData.isPlayerOwned(home)) continue;

            // Find the tenant for this home
            UUID tenant = findTenantForHome(home);
            if (tenant == null) continue;

            int withdrawn = townData.withdrawFromWallet(tenant, rent);
            if (withdrawn < rent) {
                // Cannot pay rent — revoke home
                townData.revokeHome(tenant);
                ColonyMod.LOGGER.info("[Colony] Colonist {} evicted for non-payment of rent.", tenant);
            } else {
                townData.adjustTreasury(withdrawn);
            }
        }
    }

    private UUID findTenantForHome(net.minecraft.core.BlockPos homePos) {
        for (UUID id : townData.getColonistIds()) {
            net.minecraft.core.BlockPos assigned = townData.getHome(id);
            if (homePos.equals(assigned)) return id;
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Player job application
    // -------------------------------------------------------------------------

    /**
     * Processes a player's request to take a shift at a colony workstation.
     * The player is assigned the requested role if a vacancy exists.
     *
     * @param player the applying player
     * @param role   the desired job role
     */
    public void applyPlayerForJob(ServerPlayer player, JobRole role) {
        UUID playerId = player.getUUID();

        // Register the player as a colony member if not already
        if (townData.getJob(playerId) == JobRole.UNEMPLOYED) {
            townData.addColonist(playerId);
        }

        townData.assignJob(playerId, role);
        ColonyMod.LOGGER.info("[Colony] Player {} accepted job as {}",
                player.getName().getString(), role.getDisplayName());

        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "[Colony] You are now employed as a " + role.getDisplayName() +
                ". Wages will be paid at the end of each in-game day."));
        savedData.setDirty();
    }

    // -------------------------------------------------------------------------
    // Crime event handling
    // -------------------------------------------------------------------------

    /**
     * Records the crime, blacklists the perpetrator, and dispatches guards.
     */
    public void handleCrime(CrimeCommittedEvent event) {
        townData.getLawRecord().recordCrime(
                event.getPerpetrator(),
                event.getCrimeType(),
                event.buildLogEntry()
        );
        ColonyMod.LOGGER.info("[Colony] Crime recorded: {}", event.buildLogEntry());

        // Guards with EnforceOrderGoal will pick up the blacklist on their next AI cycle.
        // (Actual guard dispatch happens in ColonistEntity AI when it checks the LawRecord.)
        savedData.setDirty();
    }

    private static final class TownSavedData extends SavedData {
        private final TownData townData;

        private TownSavedData() {
            this(new TownData());
        }

        private TownSavedData(TownData townData) {
            this.townData = townData;
        }

        private static TownSavedData load(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
            TownData townData = new TownData();
            townData.load(tag.getCompound("townData"));
            return new TownSavedData(townData);
        }

        private static Factory<TownSavedData> factory() {
            return new Factory<>(TownSavedData::new, TownSavedData::load, DataFixTypes.LEVEL);
        }

        @Override
        public CompoundTag save(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
            tag.put("townData", townData.save());
            return tag;
        }

        private TownData townData() {
            return townData;
        }
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public TownData getTownData() { return townData; }
    public ColonyStateMonitor getStateMonitor() { return stateMonitor; }
    public SimulationMode getSimulationMode() { return simulationMode; }
}
