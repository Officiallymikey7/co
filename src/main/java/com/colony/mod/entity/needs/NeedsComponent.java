package com.colony.mod.entity.needs;

import net.minecraft.nbt.CompoundTag;

import java.util.EnumMap;
import java.util.Map;

/**
 * Container that holds all {@link Need} instances for a single colonist.
 *
 * <p>This component is owned by a {@link com.colony.mod.entity.ColonistEntity} and is ticked
 * every game tick to apply natural decay to each need. The
 * {@link com.colony.mod.entity.ai.UtilityAI} reads from this component to compute action scores.
 */
public class NeedsComponent {

    private final Map<NeedType, Need> needs = new EnumMap<>(NeedType.class);

    public NeedsComponent() {
        for (NeedType type : NeedType.values()) {
            needs.put(type, new Need(type));
        }
    }

    // -------------------------------------------------------------------------
    // Tick
    // -------------------------------------------------------------------------

    /**
     * Advances every need by one game tick (applies decay).
     * Should be called from {@code ColonistEntity#tick()}.
     */
    public void tick() {
        for (Need need : needs.values()) {
            need.tick();
        }
    }

    // -------------------------------------------------------------------------
    // Access
    // -------------------------------------------------------------------------

    /**
     * Returns the {@link Need} for the given type.
     *
     * @param type the need category
     * @return the mutable need object
     */
    public Need get(NeedType type) {
        return needs.get(type);
    }

    /**
     * Returns the current value (0–100) for the given need type.
     *
     * @param type the need category
     * @return current need value
     */
    public float getValue(NeedType type) {
        return needs.get(type).getValue();
    }

    /**
     * Returns the deficit fraction (0 = full, 1 = empty) for the given need type.
     * Used by the Utility AI to score urgency.
     *
     * @param type the need category
     * @return deficit fraction in [0, 1]
     */
    public float deficitFraction(NeedType type) {
        return needs.get(type).deficitFraction();
    }

    /**
     * Satisfies the given need by {@code amount} points.
     */
    public void satisfy(NeedType type, float amount) {
        needs.get(type).satisfy(amount);
    }

    /**
     * Depletes the given need by {@code amount} points.
     */
    public void deplete(NeedType type, float amount) {
        needs.get(type).deplete(amount);
    }

    // -------------------------------------------------------------------------
    // NBT persistence
    // -------------------------------------------------------------------------

    /**
     * Serialises all need values to an NBT {@link CompoundTag}.
     */
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        for (NeedType type : NeedType.values()) {
            tag.putFloat(type.name(), needs.get(type).getValue());
        }
        return tag;
    }

    /**
     * Restores need values from a previously saved NBT {@link CompoundTag}.
     */
    public void load(CompoundTag tag) {
        for (NeedType type : NeedType.values()) {
            if (tag.contains(type.name())) {
                needs.get(type).setValue(tag.getFloat(type.name()));
            }
        }
    }
}
