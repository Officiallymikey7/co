package com.colony.mod.social;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

/**
 * Tracks the relationship between two colonists as a simple numerical value in {@code [-100, 100]}.
 *
 * <ul>
 *   <li>{@code +100} — best friends / partners (triggers cohabitation request)</li>
 *   <li>{@code   0} — neutral strangers</li>
 *   <li>{@code -100} — bitter enemies (triggers avoidance behaviour)</li>
 * </ul>
 *
 * <p>Relationship data is stored symmetrically in the {@link SocialNetwork}: the pair
 * {@code (A, B)} and {@code (B, A)} share the same {@code RelationshipData} instance.
 */
public class RelationshipData {

    public static final float MAX_VALUE =  100f;
    public static final float MIN_VALUE = -100f;

    /** Threshold at which two colonists will request to live together. */
    public static final float COHABITATION_THRESHOLD = 70f;

    /** Threshold at which two colonists actively avoid each other. */
    public static final float AVOIDANCE_THRESHOLD = -50f;

    private final UUID colonistA;
    private final UUID colonistB;
    private float affinity;

    public RelationshipData(UUID colonistA, UUID colonistB) {
        this.colonistA = colonistA;
        this.colonistB = colonistB;
        this.affinity = 0f;
    }

    // -------------------------------------------------------------------------
    // Mutation
    // -------------------------------------------------------------------------

    /**
     * Changes the affinity by {@code delta}, clamped to {@code [MIN_VALUE, MAX_VALUE]}.
     *
     * @param delta positive to improve the relationship, negative to worsen it
     */
    public void changeAffinity(float delta) {
        affinity = Math.max(MIN_VALUE, Math.min(MAX_VALUE, affinity + delta));
    }

    // -------------------------------------------------------------------------
    // State queries
    // -------------------------------------------------------------------------

    /** Returns {@code true} if affinity is high enough for the colonists to want to live together. */
    public boolean wantsCohabitation() {
        return affinity >= COHABITATION_THRESHOLD;
    }

    /** Returns {@code true} if affinity is low enough that the colonists actively avoid each other. */
    public boolean isAntagonistic() {
        return affinity <= AVOIDANCE_THRESHOLD;
    }

    // -------------------------------------------------------------------------
    // NBT persistence
    // -------------------------------------------------------------------------

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("colonistA", colonistA);
        tag.putUUID("colonistB", colonistB);
        tag.putFloat("affinity", affinity);
        return tag;
    }

    public static RelationshipData load(CompoundTag tag) {
        UUID a = tag.getUUID("colonistA");
        UUID b = tag.getUUID("colonistB");
        RelationshipData data = new RelationshipData(a, b);
        data.affinity = tag.getFloat("affinity");
        return data;
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public UUID getColonistA() { return colonistA; }
    public UUID getColonistB() { return colonistB; }
    public float getAffinity() { return affinity; }

    @Override
    public String toString() {
        return String.format("Relationship[%s<->%s affinity=%.1f]",
                colonistA.toString().substring(0, 8),
                colonistB.toString().substring(0, 8),
                affinity);
    }
}
