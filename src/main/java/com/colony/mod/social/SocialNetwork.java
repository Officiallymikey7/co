package com.colony.mod.social;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.*;

/**
 * Manages all pairwise {@link RelationshipData} records for a colony's population.
 *
 * <p>Relationships are stored symmetrically: the key is a canonical pair UUID where the
 * lower UUID always comes first. This prevents duplicates (A→B and B→A stored twice).
 *
 * <p>When affinity between two colonists crosses {@link RelationshipData#COHABITATION_THRESHOLD},
 * the network notifies the {@link com.colony.mod.town.TownPlanner} to build (or extend)
 * a shared home.
 */
public class SocialNetwork {

    /** All pairwise relationships, keyed by canonical pair key. */
    private final Map<String, RelationshipData> relationships = new HashMap<>();

    // -------------------------------------------------------------------------
    // Interaction API
    // -------------------------------------------------------------------------

    /**
     * Records a positive social interaction between two colonists, increasing their affinity.
     *
     * @param a     UUID of the first colonist
     * @param b     UUID of the second colonist
     * @param delta affinity change (positive = warmer relationship)
     * @return the updated {@link RelationshipData}
     */
    public RelationshipData interact(UUID a, UUID b, float delta) {
        RelationshipData rel = getOrCreate(a, b);
        rel.changeAffinity(delta);
        return rel;
    }

    /**
     * Returns a list of colonist UUID pairs that want to live together (affinity above threshold).
     *
     * @return pairs ready for cohabitation
     */
    public List<UUID[]> getCohabitationCandidates() {
        List<UUID[]> result = new ArrayList<>();
        for (RelationshipData rel : relationships.values()) {
            if (rel.wantsCohabitation()) {
                result.add(new UUID[]{rel.getColonistA(), rel.getColonistB()});
            }
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // Access
    // -------------------------------------------------------------------------

    /**
     * Returns the relationship between two colonists, creating a neutral one if none exists.
     *
     * @param a UUID of colonist A
     * @param b UUID of colonist B
     * @return the relationship data (never null)
     */
    public RelationshipData getOrCreate(UUID a, UUID b) {
        return relationships.computeIfAbsent(pairKey(a, b), k -> new RelationshipData(a, b));
    }

    /**
     * Returns the relationship between two colonists, or {@code null} if none has been recorded.
     *
     * @param a UUID of colonist A
     * @param b UUID of colonist B
     * @return the relationship data, or {@code null}
     */
    public RelationshipData get(UUID a, UUID b) {
        return relationships.get(pairKey(a, b));
    }

    /** Returns the total number of recorded relationships. */
    public int size() { return relationships.size(); }

    // -------------------------------------------------------------------------
    // NBT persistence
    // -------------------------------------------------------------------------

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (RelationshipData rel : relationships.values()) {
            list.add(rel.save());
        }
        tag.put("relationships", list);
        return tag;
    }

    public void load(CompoundTag tag) {
        relationships.clear();
        ListTag list = tag.getList("relationships", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            RelationshipData rel = RelationshipData.load(list.getCompound(i));
            relationships.put(pairKey(rel.getColonistA(), rel.getColonistB()), rel);
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Returns a canonical string key for the pair (a, b) that is symmetric:
     * {@code pairKey(a, b) == pairKey(b, a)}.
     */
    private static String pairKey(UUID a, UUID b) {
        // Always put the lexicographically smaller UUID first
        return a.compareTo(b) <= 0
                ? a + ":" + b
                : b + ":" + a;
    }
}
