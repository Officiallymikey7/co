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
 * and the {@link SocialNetwork} and {@link SmartObjectRegistry} for this dimension.
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
    }

    public int getPopulation() { return colonistIds.size(); }
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

    // -------------------------------------------------------------------------
    // Housing
    // -------------------------------------------------------------------------

    public void assignHome(UUID colonistId, BlockPos homePos) {
        homeAssignments.put(colonistId, homePos.immutable());
    }

    public BlockPos getHome(UUID colonistId) {
        return homeAssignments.get(colonistId);
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

        // Social network
        tag.put("socialNetwork", socialNetwork.save());

        return tag;
    }

    public void load(CompoundTag tag) {
        townName = tag.getString("townName");
        townCenter = BlockPos.of(tag.getLong("townCenter"));
        foodStoreLevel = tag.getInt("foodStore");
        defenceLevel = tag.getInt("defenceLevel");
        housingCapacity = tag.getInt("housingCapacity");

        colonistIds.clear();
        homeAssignments.clear();
        jobAssignments.clear();

        ListTag colonistList = tag.getList("colonists", Tag.TAG_COMPOUND);
        for (int i = 0; i < colonistList.size(); i++) {
            CompoundTag c = colonistList.getCompound(i);
            UUID id = c.getUUID("id");
            colonistIds.add(id);
            jobAssignments.put(id, JobRole.valueOf(c.getString("job")));
            if (c.contains("home")) {
                homeAssignments.put(id, BlockPos.of(c.getLong("home")));
            }
        }

        if (tag.contains("socialNetwork")) {
            socialNetwork.load(tag.getCompound("socialNetwork"));
        }
    }
}
