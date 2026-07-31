package com.colony.mod.town;

import net.minecraft.nbt.CompoundTag;

/**
 * A single tax-rate proposal considered during a council vote cycle.
 *
 * <p>Proposals are generated randomly within the config-defined min/max range.
 * Each {@link JobRole#MERCHANT} councillor "votes" by affinity weight, and the proposal with
 * the most weighted votes wins and is applied to {@link LawRecord#taxRate}.
 */
public class VoteProposal {

    private final double proposedTaxRate;
    private double voteWeight;

    public VoteProposal(double proposedTaxRate) {
        this.proposedTaxRate = proposedTaxRate;
        this.voteWeight = 0.0;
    }

    /** Adds weighted support for this proposal. */
    public void addVote(double weight) {
        voteWeight += weight;
    }

    public double getProposedTaxRate() { return proposedTaxRate; }
    public double getVoteWeight() { return voteWeight; }

    // -------------------------------------------------------------------------
    // NBT persistence
    // -------------------------------------------------------------------------

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("rate", proposedTaxRate);
        tag.putDouble("weight", voteWeight);
        return tag;
    }

    public static VoteProposal load(CompoundTag tag) {
        VoteProposal p = new VoteProposal(tag.getDouble("rate"));
        p.voteWeight = tag.getDouble("weight");
        return p;
    }

    @Override
    public String toString() {
        return String.format("VoteProposal[rate=%.2f, weight=%.1f]", proposedTaxRate, voteWeight);
    }
}
