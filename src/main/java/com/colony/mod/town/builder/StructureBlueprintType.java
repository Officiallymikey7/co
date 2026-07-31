package com.colony.mod.town.builder;

/**
 * Enumeration of pre-made structure blueprints that the autonomous town planner can order.
 *
 * <p>Each type maps to an NBT structure template stored under
 * {@code data/colony/structures/<name>.nbt}.
 */
public enum StructureBlueprintType {

    /** A small 1–2 person house; increases housing capacity by 2. */
    SMALL_HOUSE("small_house", 2, 60),

    /** A large house for an established family; increases housing capacity by 4. */
    LARGE_HOUSE("large_house", 4, 120),

    /** An open-air farm plot with soil, water channel, and crop rows. */
    FARM("farm", 0, 80),

    /** A simple guard post with a torch and fence perimeter. */
    GUARD_POST("guard_post", 0, 50),

    /** A central town hall / notice board hub. */
    TOWN_HALL("town_hall", 0, 200),

    /** A bakery with an oven smart-object; improves food satisfaction. */
    BAKERY("bakery", 0, 100);

    /** Filename (without .nbt) of the template stored in resources. */
    private final String templateName;

    /** How much this structure increases the colony's housing capacity when complete. */
    private final int housingCapacityBonus;

    /** Approximate number of builder ticks required to complete the structure. */
    private final int estimatedBuildTicks;

    StructureBlueprintType(String templateName, int housingCapacityBonus, int estimatedBuildTicks) {
        this.templateName = templateName;
        this.housingCapacityBonus = housingCapacityBonus;
        this.estimatedBuildTicks = estimatedBuildTicks;
    }

    public String getTemplateName() { return templateName; }
    public int getHousingCapacityBonus() { return housingCapacityBonus; }
    public int getEstimatedBuildTicks() { return estimatedBuildTicks; }
}
