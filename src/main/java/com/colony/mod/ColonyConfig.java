package com.colony.mod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * JSON config for the Colony mod.
 *
 * <p>All tunable values are exposed here so server admins and modpack builders can tweak the
 * simulation without touching code. The config is stored in {@code config/colony-common.json}.
 * Sections mirror the four areas of the mod:
 *
 * <ul>
 *   <li>{@code needs} — per-need decay rate multiplier</li>
 *   <li>{@code construction} — build speed and concurrency limits</li>
 *   <li>{@code town} — population and economic parameters</li>
 *   <li>{@code performance} — async AI and abstract-sim tuning</li>
 * </ul>
 */
public final class ColonyConfig {

    private static final Logger LOGGER = LogManager.getLogger(ColonyMod.MOD_ID);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE = ColonyMod.MOD_ID + "-common.json";

    private static ConfigData data = new ConfigData();

    private ColonyConfig() {}

    /** Loads config from disk (or writes defaults if the file doesn't exist). */
    public static void load() {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE);
        if (Files.exists(configPath)) {
            try (Reader reader = Files.newBufferedReader(configPath)) {
                ConfigData loaded = GSON.fromJson(reader, ConfigData.class);
                if (loaded != null) data = loaded;
            } catch (IOException e) {
                LOGGER.error("[Colony] Failed to read config, using defaults", e);
            }
        }
        save(configPath);
    }

    private static void save(Path configPath) {
        try {
            Files.createDirectories(configPath.getParent());
        } catch (IOException ignored) {}
        try (Writer writer = Files.newBufferedWriter(configPath)) {
            GSON.toJson(data, writer);
        } catch (IOException e) {
            LOGGER.error("[Colony] Failed to write config", e);
        }
    }

    // -------------------------------------------------------------------------
    // Config data container (fields match the old ModConfigSpec names)
    // -------------------------------------------------------------------------

    private static class ConfigData {
        /** Global multiplier applied to every need's per-tick decay rate (default 1.0). */
        double needDecayMultiplier = 1.0;
        /** Multiplier on the number of ticks it takes a builder to complete one block (default 1.0). */
        double buildSpeedMultiplier = 1.0;
        /** Maximum number of simultaneously active builder tasks (default 3). */
        int maxConcurrentBuildTasks = 3;
        /** Housing capacity the colony starts with before any buildings are placed (default 5). */
        int startingHousingCapacity = 5;
        /** Hard cap on colony population (default 100). */
        int maxPopulation = 100;
        /** Minimum tax rate the council may vote in (default 0.0). */
        double minTaxRate = 0.0;
        /** Maximum tax rate the council may vote in (default 0.3). */
        double maxTaxRate = 0.3;
        /** How many in-game days between council tax votes (default 7). */
        int taxVoteIntervalDays = 7;
        /** Base daily wage paid to each employed colonist (default 10). */
        int baseDailyWage = 10;
        /** Need-point delta that triggers an AI replan (default 10). */
        int aiReplanNeedThreshold = 10;
        /** How often (in server ticks) the Colony State Monitor runs (default 200). */
        int colonyCheckIntervalTicks = 200;
        /** How often (in server ticks) the abstract simulation updates an unloaded colony (default 1200). */
        int abstractSimTickInterval = 1200;
    }

    // -------------------------------------------------------------------------
    // Convenience accessors
    // -------------------------------------------------------------------------

    public static float getNeedDecayMultiplier() {
        return (float) data.needDecayMultiplier;
    }

    public static int getColonyCheckIntervalTicks() {
        return data.colonyCheckIntervalTicks;
    }

    public static int getAbstractSimTickInterval() {
        return data.abstractSimTickInterval;
    }

    public static int getAiReplanNeedThreshold() {
        return data.aiReplanNeedThreshold;
    }

    public static int getTaxVoteIntervalDays() {
        return data.taxVoteIntervalDays;
    }

    public static int getBaseDailyWage() {
        return data.baseDailyWage;
    }

    public static double getMinTaxRate() {
        return data.minTaxRate;
    }

    public static double getMaxTaxRate() {
        return data.maxTaxRate;
    }

    public static int getMaxConcurrentBuildTasks() {
        return data.maxConcurrentBuildTasks;
    }

    public static int getMaxPopulation() {
        return data.maxPopulation;
    }
}
