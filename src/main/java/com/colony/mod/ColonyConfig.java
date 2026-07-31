package com.colony.mod;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * NeoForge TOML config for the Colony mod.
 *
 * <p>All tunable values are exposed here so server admins and modpack builders can tweak the
 * simulation without touching code. Sections mirror the four areas of the mod:
 *
 * <ul>
 *   <li>{@code [needs]} — per-need decay rate multiplier</li>
 *   <li>{@code [construction]} — build speed and concurrency limits</li>
 *   <li>{@code [town]} — population and economic parameters</li>
 *   <li>{@code [performance]} — async AI and abstract-sim tuning</li>
 * </ul>
 */
public final class ColonyConfig {

    /** The built spec, registered with the mod container. */
    public static final ModConfigSpec SPEC;

    // -------------------------------------------------------------------------
    // [needs]
    // -------------------------------------------------------------------------

    /** Global multiplier applied to every need's per-tick decay rate (default 1.0). */
    public static final ModConfigSpec.DoubleValue NEED_DECAY_MULTIPLIER;

    // -------------------------------------------------------------------------
    // [construction]
    // -------------------------------------------------------------------------

    /** Multiplier on the number of ticks it takes a builder to complete one block (default 1.0). */
    public static final ModConfigSpec.DoubleValue BUILD_SPEED_MULTIPLIER;

    /** Maximum number of simultaneously active builder tasks (default 3). */
    public static final ModConfigSpec.IntValue MAX_CONCURRENT_BUILD_TASKS;

    // -------------------------------------------------------------------------
    // [town]
    // -------------------------------------------------------------------------

    /** Housing capacity the colony starts with before any buildings are placed (default 5). */
    public static final ModConfigSpec.IntValue STARTING_HOUSING_CAPACITY;

    /** Hard cap on colony population — the planner will not build new houses beyond this (default 100). */
    public static final ModConfigSpec.IntValue MAX_POPULATION;

    /** Minimum tax rate the council may vote in (0.0–1.0, default 0.0). */
    public static final ModConfigSpec.DoubleValue MIN_TAX_RATE;

    /** Maximum tax rate the council may vote in (0.0–1.0, default 0.3). */
    public static final ModConfigSpec.DoubleValue MAX_TAX_RATE;

    /** How many in-game days between council tax votes (default 7). */
    public static final ModConfigSpec.IntValue TAX_VOTE_INTERVAL_DAYS;

    /** Base daily wage paid to each employed colonist (in colony coins, default 10). */
    public static final ModConfigSpec.IntValue BASE_DAILY_WAGE;

    // -------------------------------------------------------------------------
    // [performance]
    // -------------------------------------------------------------------------

    /**
     * Need-point delta that triggers an AI replan (default 10).
     * Lower values make colonists more responsive; higher values reduce CPU cost.
     */
    public static final ModConfigSpec.IntValue AI_REPLAN_NEED_THRESHOLD;

    /**
     * How often (in server ticks) the Colony State Monitor runs its evaluation pass (default 200).
     */
    public static final ModConfigSpec.IntValue COLONY_CHECK_INTERVAL_TICKS;

    /**
     * How often (in server ticks) the abstract simulation updates an unloaded colony (default 1200,
     * i.e. once per real-time minute at 20 TPS).
     */
    public static final ModConfigSpec.IntValue ABSTRACT_SIM_TICK_INTERVAL;

    // -------------------------------------------------------------------------
    // Static initialiser — builds the spec
    // -------------------------------------------------------------------------

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("Need decay settings").push("needs");
        NEED_DECAY_MULTIPLIER = builder
                .comment("Global multiplier on all need decay rates. 1.0 = default, 2.0 = twice as fast.")
                .defineInRange("needDecayMultiplier", 1.0, 0.01, 10.0);
        builder.pop();

        builder.comment("Construction settings").push("construction");
        BUILD_SPEED_MULTIPLIER = builder
                .comment("Multiplier on builder tick duration. 1.0 = default, 0.5 = twice as fast.")
                .defineInRange("buildSpeedMultiplier", 1.0, 0.1, 10.0);
        MAX_CONCURRENT_BUILD_TASKS = builder
                .comment("Maximum number of active builder tasks at once.")
                .defineInRange("maxConcurrentBuildTasks", 3, 1, 20);
        builder.pop();

        builder.comment("Town and economy settings").push("town");
        STARTING_HOUSING_CAPACITY = builder
                .comment("Initial housing capacity before any houses are built.")
                .defineInRange("startingHousingCapacity", 5, 1, 500);
        MAX_POPULATION = builder
                .comment("Hard population cap. Town Planner will not build houses beyond this.")
                .defineInRange("maxPopulation", 100, 1, 1000);
        MIN_TAX_RATE = builder
                .comment("Minimum tax rate the NPC council can vote in (fraction 0.0–1.0).")
                .defineInRange("minTaxRate", 0.0, 0.0, 1.0);
        MAX_TAX_RATE = builder
                .comment("Maximum tax rate the NPC council can vote in (fraction 0.0–1.0).")
                .defineInRange("maxTaxRate", 0.3, 0.0, 1.0);
        TAX_VOTE_INTERVAL_DAYS = builder
                .comment("In-game days between council tax-rate votes.")
                .defineInRange("taxVoteIntervalDays", 7, 1, 365);
        BASE_DAILY_WAGE = builder
                .comment("Base colony coins paid daily to each employed colonist (UNEMPLOYED earns 0).")
                .defineInRange("baseDailyWage", 10, 0, 10000);
        builder.pop();

        builder.comment("Performance and simulation settings").push("performance");
        AI_REPLAN_NEED_THRESHOLD = builder
                .comment("Need-point change required to trigger an async AI replan.")
                .defineInRange("aiReplanNeedThreshold", 10, 1, 100);
        COLONY_CHECK_INTERVAL_TICKS = builder
                .comment("Ticks between Colony State Monitor evaluation passes (20 ticks = 1 second).")
                .defineInRange("colonyCheckIntervalTicks", 200, 20, 24000);
        ABSTRACT_SIM_TICK_INTERVAL = builder
                .comment("Ticks between abstract-simulation updates for unloaded colonies.")
                .defineInRange("abstractSimTickInterval", 1200, 20, 72000);
        builder.pop();

        SPEC = builder.build();
    }

    private ColonyConfig() {}

    // -------------------------------------------------------------------------
    // Convenience accessors (safe to call before config is loaded)
    // -------------------------------------------------------------------------

    public static float getNeedDecayMultiplier() {
        return SPEC.isLoaded() ? (float) NEED_DECAY_MULTIPLIER.get().doubleValue() : 1.0f;
    }

    public static int getColonyCheckIntervalTicks() {
        return SPEC.isLoaded() ? COLONY_CHECK_INTERVAL_TICKS.get() : 200;
    }

    public static int getAbstractSimTickInterval() {
        return SPEC.isLoaded() ? ABSTRACT_SIM_TICK_INTERVAL.get() : 1200;
    }

    public static int getAiReplanNeedThreshold() {
        return SPEC.isLoaded() ? AI_REPLAN_NEED_THRESHOLD.get() : 10;
    }

    public static int getTaxVoteIntervalDays() {
        return SPEC.isLoaded() ? TAX_VOTE_INTERVAL_DAYS.get() : 7;
    }

    public static int getBaseDailyWage() {
        return SPEC.isLoaded() ? BASE_DAILY_WAGE.get() : 10;
    }

    public static double getMinTaxRate() {
        return SPEC.isLoaded() ? MIN_TAX_RATE.get() : 0.0;
    }

    public static double getMaxTaxRate() {
        return SPEC.isLoaded() ? MAX_TAX_RATE.get() : 0.3;
    }

    public static int getMaxConcurrentBuildTasks() {
        return SPEC.isLoaded() ? MAX_CONCURRENT_BUILD_TASKS.get() : 3;
    }
}
