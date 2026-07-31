package com.colony.mod.town;

/**
 * Enumeration of every job role a colonist can hold within the colony.
 *
 * <p>Job roles are dynamically assigned: if the holder of a role dies or becomes unemployed,
 * the nearest idle colonist automatically claims the vacant workstation.
 */
public enum JobRole {

    UNEMPLOYED("Unemployed"),
    FARMER("Farmer"),
    BUILDER("Builder"),
    GUARD("Guard"),
    COOK("Cook"),
    LUMBERJACK("Lumberjack"),
    MINER("Miner"),
    MERCHANT("Merchant"),
    DOCTOR("Doctor");

    private final String displayName;

    JobRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }
}
