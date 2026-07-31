package com.colony.mod.event;

import com.colony.mod.town.CrimeType;
import net.minecraft.world.entity.Entity;

import java.util.UUID;

/**
 * Represents a crime event committed against the colony.
 *
 * <p>Created when an entity commits a crime (e.g. assaulting a colonist) and passed to
 * {@link com.colony.mod.town.TownManager#handleCrime} for recording and guard dispatch.
 */
public class CrimeCommittedEvent {

    private final UUID perpetratorId;
    private final CrimeType crimeType;
    private final Entity perpetratorEntity;

    /**
     * @param perpetratorEntity the entity that committed the crime (player or NPC)
     * @param crimeType         the category of crime
     */
    public CrimeCommittedEvent(Entity perpetratorEntity, CrimeType crimeType) {
        this.perpetratorEntity = perpetratorEntity;
        this.perpetratorId = perpetratorEntity.getUUID();
        this.crimeType = crimeType;
    }

    /** UUID of the perpetrator — safe to use after the entity despawns. */
    public UUID getPerpetrator() { return perpetratorId; }

    /** The live entity reference (may be null after despawn; check {@code isAlive()}). */
    public Entity getPerpetratorEntity() { return perpetratorEntity; }

    /** @deprecated Use {@link #getPerpetratorEntity()}. */
    @Deprecated(forRemoval = true)
    public Entity getPerpetatorEntity() { return getPerpetratorEntity(); }

    public CrimeType getCrimeType() { return crimeType; }

    /** Human-readable description for the colony crime log. */
    public String buildLogEntry() {
        String name = perpetratorEntity.getName().getString();
        return name + " committed " + crimeType.getDisplayName();
    }
}
