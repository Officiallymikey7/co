package com.colony.mod.town;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.*;

/**
 * Persistent record of the colony's laws, ongoing tax vote, and crime history.
 *
 * <p>Stored inside {@link TownData} and serialised with the world. The
 * {@link ColonyStateMonitor} triggers a new vote cycle every N in-game days; guard colonists
 * reference the crime blacklist to decide whether to pursue a given entity.
 */
public class LawRecord {

    /** Current tax rate applied to player wages (0.0 = no tax, 1.0 = 100% tax). */
    private double taxRate = 0.0;

    /**
     * UUIDs of entities that have committed a crime and are wanted by the colony guard.
     * Entries persist until the player pays a fine or serves their time.
     */
    private final Set<UUID> crimeBlacklist = new HashSet<>();

    /**
     * Tax-rate proposals currently open for a vote.  Empty when no vote is in progress.
     */
    private final List<VoteProposal> pendingVotes = new ArrayList<>();

    /**
     * Rolling log of the most recent crimes committed (capped at {@link #MAX_CRIME_LOG}).
     * Each entry is a human-readable description (e.g. "PlayerName committed Theft").
     */
    private final Deque<String> crimeLog = new ArrayDeque<>();
    private static final int MAX_CRIME_LOG = 50;

    // -------------------------------------------------------------------------
    // Crime management
    // -------------------------------------------------------------------------

    /**
     * Records a crime and adds the perpetrator to the blacklist.
     *
     * @param perpetratorId UUID of the offending entity
     * @param type          the type of crime committed
     * @param description   human-readable log entry
     */
    public void recordCrime(UUID perpetratorId, CrimeType type, String description) {
        crimeBlacklist.add(perpetratorId);
        if (crimeLog.size() >= MAX_CRIME_LOG) crimeLog.pollFirst();
        crimeLog.addLast(description);
    }

    /** Removes a perpetrator from the blacklist (e.g. fine paid). */
    public void pardon(UUID perpetratorId) {
        crimeBlacklist.remove(perpetratorId);
    }

    public boolean isWanted(UUID id) {
        return crimeBlacklist.contains(id);
    }

    /**
     * Returns the most recent {@code n} crime log entries, newest last.
     *
     * @param n maximum entries to return
     * @return an unmodifiable list of log strings
     */
    public List<String> getRecentCrimes(int n) {
        List<String> all = new ArrayList<>(crimeLog);
        int start = Math.max(0, all.size() - n);
        return Collections.unmodifiableList(all.subList(start, all.size()));
    }

    // -------------------------------------------------------------------------
    // Tax / voting
    // -------------------------------------------------------------------------

    public double getTaxRate() { return taxRate; }
    public void setTaxRate(double rate) { taxRate = Math.max(0.0, Math.min(1.0, rate)); }

    /** Clears existing proposals and opens a new vote with the supplied options. */
    public void openVote(List<VoteProposal> proposals) {
        pendingVotes.clear();
        pendingVotes.addAll(proposals);
    }

    /** Closes the vote, applying the winning proposal. */
    public void closeVote() {
        if (pendingVotes.isEmpty()) return;
        VoteProposal winner = pendingVotes.stream()
                .max(Comparator.comparingDouble(VoteProposal::getVoteWeight))
                .orElse(null);
        if (winner != null) {
            taxRate = winner.getProposedTaxRate();
        }
        pendingVotes.clear();
    }

    public List<VoteProposal> getPendingVotes() {
        return Collections.unmodifiableList(pendingVotes);
    }

    public boolean isVoteInProgress() { return !pendingVotes.isEmpty(); }

    // -------------------------------------------------------------------------
    // NBT persistence
    // -------------------------------------------------------------------------

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("taxRate", taxRate);

        // Blacklist
        ListTag blacklistTag = new ListTag();
        for (UUID id : crimeBlacklist) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("id", id);
            blacklistTag.add(entry);
        }
        tag.put("blacklist", blacklistTag);

        // Crime log
        ListTag logTag = new ListTag();
        for (String entry : crimeLog) {
            CompoundTag e = new CompoundTag();
            e.putString("entry", entry);
            logTag.add(e);
        }
        tag.put("crimeLog", logTag);

        // Pending votes
        ListTag votesTag = new ListTag();
        for (VoteProposal vp : pendingVotes) {
            votesTag.add(vp.save());
        }
        tag.put("pendingVotes", votesTag);

        return tag;
    }

    public void load(CompoundTag tag) {
        taxRate = tag.getDouble("taxRate");

        crimeBlacklist.clear();
        ListTag blacklistTag = tag.getList("blacklist", Tag.TAG_COMPOUND);
        for (int i = 0; i < blacklistTag.size(); i++) {
            crimeBlacklist.add(blacklistTag.getCompound(i).getUUID("id"));
        }

        crimeLog.clear();
        ListTag logTag = tag.getList("crimeLog", Tag.TAG_COMPOUND);
        for (int i = 0; i < logTag.size(); i++) {
            crimeLog.addLast(logTag.getCompound(i).getString("entry"));
        }

        pendingVotes.clear();
        ListTag votesTag = tag.getList("pendingVotes", Tag.TAG_COMPOUND);
        for (int i = 0; i < votesTag.size(); i++) {
            pendingVotes.add(VoteProposal.load(votesTag.getCompound(i)));
        }
    }
}
