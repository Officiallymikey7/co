package com.colony.mod.town;

import com.colony.mod.smartobject.SmartObjectRegistry;
import com.colony.mod.social.SocialNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.*;

/**
 * Persistent data container for a single colony.
 *
 * <p>Stores the colony's current census, housing assignments, food reserves, defence level,
 * economy (treasury, wages, player wallets), housing market (rent prices), law record, and the
 * {@link SocialNetwork} and {@link SmartObjectRegistry} for this dimension.
 *
 * <p>This class is serialised with the world (via a {@code SavedData} subclass) so that
 * colony state persists across server restarts.
 */
public class TownData {

    // -------------------------------------------------------------------------
    // Colony identity
    // -------------------------------------------------------------------------

    private String townName = "New Colony";
    private BlockPos townCenter = BlockPos.ZERO;

    // -------------------------------------------------------------------------
    // Demographics
    // -------------------------------------------------------------------------

    /** UUIDs of all living colonists. */
    private final Set<UUID> colonistIds = new HashSet<>();

    /** Maps colonist UUID → their assigned home position (null if homeless). */
    private final Map<UUID, BlockPos> homeAssignments = new HashMap<>();

    /** Maps colonist UUID → their current job role. */
    private final Map<UUID, JobRole> jobAssignments = new HashMap<>();

    // -------------------------------------------------------------------------
    // Resources / metrics
    // -------------------------------------------------------------------------

    /** Current food store level (arbitrary units, 0 = starving). */
    private int foodStoreLevel = 100;

    /** Current defence level (0 = undefended). */
    private int defenceLevel = 0;

    /** Maximum number of colonists the current housing can support. */
    private int housingCapacity = 5;

    // -------------------------------------------------------------------------
    // Economy — treasury, wages, player wallets
    // -------------------------------------------------------------------------

    /** Shared colony treasury (colony coins). Payday withdrawals come from here. */
    private int townTreasury = 0;

    /**
     * Per-role daily wage override.  Falls back to {@link com.colony.mod.ColonyConfig#getBaseDailyWage()}
     * when no override is set for a role.
     */
    private final Map<JobRole, Integer> colonistWages = new EnumMap<>(JobRole.class);

    /**
     * Per-UUID wallet balance.  Used for both NPC colonists and human players.
     * Human players can withdraw coins as {@link com.colony.mod.registry.ColonyItems#COLONY_CURRENCY}.
     */
    private final Map<UUID, Integer> walletBalances = new HashMap<>();

    // -------------------------------------------------------------------------
    // Housing market
    // -------------------------------------------------------------------------

    /** Maps a home BlockPos → its nightly rent price (0 = player-owned, no rent). */
    private final Map<BlockPos, Integer> rentPrices = new HashMap<>();

    /** Homes that a player has fully purchased (no nightly rent). */
    private final Set<BlockPos> playerOwnedHomes = new HashSet<>();

    // -------------------------------------------------------------------------
    // Law
    // -------------------------------------------------------------------------

    private final LawRecord lawRecord = new LawRecord();

    // -------------------------------------------------------------------------
    // Sub-systems
    // -------------------------------------------------------------------------

    private final SocialNetwork socialNetwork = new SocialNetwork();
    private final SmartObjectRegistry smartObjectRegistry = new SmartObjectRegistry();

    // -------------------------------------------------------------------------
    // Population management
    // -------------------------------------------------------------------------

    public void addColonist(UUID id) {
        colonistIds.add(id);
        jobAssignments.put(id, JobRole.UNEMPLOYED);
    }

    public void removeColonist(UUID id) {
        colonistIds.remove(id);
        homeAssignments.remove(id);
        jobAssignments.remove(id);
        walletBalances.remove(id);
    }

    public int getPopulation() { return colonistIds.size(); }
    public Set<UUID> getColonistIds() { return Collections.unmodifiableSet(colonistIds); }
    public int getHomelessCount() {
        int homeless = 0;
        for (UUID id : colonistIds) {
            if (!homeAssignments.containsKey(id)) homeless++;
        }
        return homeless;
    }

    // -------------------------------------------------------------------------
    // Job management
    // -------------------------------------------------------------------------

    public void assignJob(UUID colonistId, JobRole role) {
        jobAssignments.put(colonistId, role);
    }

    public JobRole getJob(UUID colonistId) {
        return jobAssignments.getOrDefault(colonistId, JobRole.UNEMPLOYED);
    }

    /** Returns UUIDs of all colonists without a real job (UNEMPLOYED). */
    public List<UUID> getUnemployed() {
        List<UUID> list = new ArrayList<>();
        for (Map.Entry<UUID, JobRole> e : jobAssignments.entrySet()) {
            if (e.getValue() == JobRole.UNEMPLOYED) list.add(e.getKey());
        }
        return list;
    }

    /** Returns the number of colonists (or players) currently holding the given role. */
    public long countByRole(JobRole role) {
        return jobAssignments.values().stream().filter(r -> r == role).count();
    }

    // -------------------------------------------------------------------------
    // Housing
    // -------------------------------------------------------------------------

    public void assignHome(UUID colonistId, BlockPos homePos) {
        homeAssignments.put(colonistId, homePos.immutable());
    }

    public BlockPos getHome(UUID colonistId) {
        return homeAssignments.get(colonistId);
    }

    /** Removes a home assignment for the given colonist (they become homeless). */
    public void revokeHome(UUID colonistId) {
        homeAssignments.remove(colonistId);
    }

    // -------------------------------------------------------------------------
    // Housing market
    // -------------------------------------------------------------------------

    /** Sets the nightly rent for a home block position. */
    public void setRentPrice(BlockPos pos, int price) {
        rentPrices.put(pos.immutable(), Math.max(0, price));
    }

    public int getRentPrice(BlockPos pos) {
        return rentPrices.getOrDefault(pos.immutable(), 0);
    }

    /** Marks a home as player-owned (no more rent payments required). */
    public void markPlayerOwned(BlockPos pos) {
        playerOwnedHomes.add(pos.immutable());
        rentPrices.remove(pos.immutable());
    }

    public boolean isPlayerOwned(BlockPos pos) {
        return playerOwnedHomes.contains(pos.immutable());
    }

    /** Returns an unmodifiable view of all homes that are available for rent or purchase. */
    public Map<BlockPos, Integer> getRentPrices() {
        return Collections.unmodifiableMap(rentPrices);
    }

    // -------------------------------------------------------------------------
    // Economy
    // -------------------------------------------------------------------------

    public int getTownTreasury() { return townTreasury; }
    public void setTownTreasury(int amount) { townTreasury = Math.max(0, amount); }
    public void adjustTreasury(int delta) { townTreasury = Math.max(0, townTreasury + delta); }

    /**
     * Returns the configured daily wage for a role.
     * Falls back to {@link com.colony.mod.ColonyConfig#getBaseDailyWage()} if no override.
     */
    public int getWageForRole(JobRole role) {
        return colonistWages.getOrDefault(role, com.colony.mod.ColonyConfig.getBaseDailyWage());
    }

    public void setWageForRole(JobRole role, int wage) {
        colonistWages.put(role, Math.max(0, wage));
    }

    /** Returns the wallet balance for a UUID (defaults to 0 if no entry exists). */
    public int getWalletBalance(UUID id) {
        return walletBalances.getOrDefault(id, 0);
    }

    /** Deposits coins into a wallet. Returns the new balance. */
    public int depositToWallet(UUID id, int amount) {
        int newBalance = walletBalances.getOrDefault(id, 0) + Math.max(0, amount);
        walletBalances.put(id, newBalance);
        return newBalance;
    }

    /**
     * Withdraws coins from a wallet. Returns the amount actually withdrawn
     * (may be less than requested if the wallet has insufficient funds).
     */
    public int withdrawFromWallet(UUID id, int amount) {
        int current = walletBalances.getOrDefault(id, 0);
        int withdrawn = Math.min(current, Math.max(0, amount));
        walletBalances.put(id, current - withdrawn);
        return withdrawn;
    }

    // -------------------------------------------------------------------------
    // Metrics
    // -------------------------------------------------------------------------

    public int getFoodStoreLevel() { return foodStoreLevel; }
    public void setFoodStoreLevel(int level) { foodStoreLevel = Math.max(0, level); }
    public void adjustFood(int delta) { foodStoreLevel = Math.max(0, foodStoreLevel + delta); }

    public int getDefenceLevel() { return defenceLevel; }
    public void setDefenceLevel(int level) { defenceLevel = Math.max(0, level); }

    public int getHousingCapacity() { return housingCapacity; }
    public void setHousingCapacity(int capacity) { housingCapacity = Math.max(0, capacity); }

    // -------------------------------------------------------------------------
    // Law
    // -------------------------------------------------------------------------

    public LawRecord getLawRecord() { return lawRecord; }

    // -------------------------------------------------------------------------
    // Sub-system accessors
    // -------------------------------------------------------------------------

    public SocialNetwork getSocialNetwork() { return socialNetwork; }
    public SmartObjectRegistry getSmartObjectRegistry() { return smartObjectRegistry; }

    public BlockPos getTownCenter() { return townCenter; }
    public void setTownCenter(BlockPos pos) { townCenter = pos.immutable(); }

    public String getTownName() { return townName; }
    public void setTownName(String name) { townName = name; }

    // -------------------------------------------------------------------------
    // NBT persistence
    // -------------------------------------------------------------------------

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("townName", townName);
        tag.putLong("townCenter", townCenter.asLong());
        tag.putInt("foodStore", foodStoreLevel);
        tag.putInt("defenceLevel", defenceLevel);
        tag.putInt("housingCapacity", housingCapacity);
        tag.putInt("treasury", townTreasury);

        // Colonists
        ListTag colonistList = new ListTag();
        for (UUID id : colonistIds) {
            CompoundTag c = new CompoundTag();
            c.putUUID("id", id);
            c.putString("job", jobAssignments.getOrDefault(id, JobRole.UNEMPLOYED).name());
            if (homeAssignments.containsKey(id)) {
                c.putLong("home", homeAssignments.get(id).asLong());
            }
            colonistList.add(c);
        }
        tag.put("colonists", colonistList);

        // Wallets
        ListTag walletList = new ListTag();
        for (Map.Entry<UUID, Integer> e : walletBalances.entrySet()) {
            CompoundTag w = new CompoundTag();
            w.putUUID("id", e.getKey());
            w.putInt("balance", e.getValue());
            walletList.add(w);
        }
        tag.put("wallets", walletList);

        // Role wages
        ListTag wageList = new ListTag();
        for (Map.Entry<JobRole, Integer> e : colonistWages.entrySet()) {
            CompoundTag w = new CompoundTag();
            w.putString("role", e.getKey().name());
            w.putInt("wage", e.getValue());
            wageList.add(w);
        }
        tag.put("wages", wageList);

        // Rent prices
        ListTag rentList = new ListTag();
        for (Map.Entry<BlockPos, Integer> e : rentPrices.entrySet()) {
            CompoundTag r = new CompoundTag();
            r.putLong("pos", e.getKey().asLong());
            r.putInt("price", e.getValue());
            rentList.add(r);
        }
        tag.put("rentPrices", rentList);

        // Player-owned homes
        ListTag ownedList = new ListTag();
        for (BlockPos pos : playerOwnedHomes) {
            CompoundTag o = new CompoundTag();
            o.putLong("pos", pos.asLong());
            ownedList.add(o);
        }
        tag.put("playerOwnedHomes", ownedList);

        // Social network
        tag.put("socialNetwork", socialNetwork.save());

        // Law record
        tag.put("lawRecord", lawRecord.save());

        return tag;
    }

    public void load(CompoundTag tag) {
        townName = tag.getString("townName");
        townCenter = BlockPos.of(tag.getLong("townCenter"));
        foodStoreLevel = tag.getInt("foodStore");
        defenceLevel = tag.getInt("defenceLevel");
        housingCapacity = tag.getInt("housingCapacity");
        townTreasury = tag.getInt("treasury");

        colonistIds.clear();
        homeAssignments.clear();
        jobAssignments.clear();

        ListTag colonistList = tag.getList("colonists", Tag.TAG_COMPOUND);
        for (int i = 0; i < colonistList.size(); i++) {
            CompoundTag c = colonistList.getCompound(i);
            UUID id = c.getUUID("id");
            colonistIds.add(id);
            try {
                jobAssignments.put(id, JobRole.valueOf(c.getString("job")));
            } catch (IllegalArgumentException e) {
                jobAssignments.put(id, JobRole.UNEMPLOYED);
            }
            if (c.contains("home")) {
                homeAssignments.put(id, BlockPos.of(c.getLong("home")));
            }
        }

        walletBalances.clear();
        ListTag walletList = tag.getList("wallets", Tag.TAG_COMPOUND);
        for (int i = 0; i < walletList.size(); i++) {
            CompoundTag w = walletList.getCompound(i);
            walletBalances.put(w.getUUID("id"), w.getInt("balance"));
        }

        colonistWages.clear();
        ListTag wageList = tag.getList("wages", Tag.TAG_COMPOUND);
        for (int i = 0; i < wageList.size(); i++) {
            CompoundTag w = wageList.getCompound(i);
            try {
                colonistWages.put(JobRole.valueOf(w.getString("role")), w.getInt("wage"));
            } catch (IllegalArgumentException ignored) {
                // unknown role — skip
            }
        }

        rentPrices.clear();
        ListTag rentList = tag.getList("rentPrices", Tag.TAG_COMPOUND);
        for (int i = 0; i < rentList.size(); i++) {
            CompoundTag r = rentList.getCompound(i);
            rentPrices.put(BlockPos.of(r.getLong("pos")), r.getInt("price"));
        }

        playerOwnedHomes.clear();
        ListTag ownedList = tag.getList("playerOwnedHomes", Tag.TAG_COMPOUND);
        for (int i = 0; i < ownedList.size(); i++) {
            playerOwnedHomes.add(BlockPos.of(ownedList.getCompound(i).getLong("pos")));
        }

        if (tag.contains("socialNetwork")) {
            socialNetwork.load(tag.getCompound("socialNetwork"));
        }

        if (tag.contains("lawRecord")) {
            lawRecord.load(tag.getCompound("lawRecord"));
        }
    }
}
