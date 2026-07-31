package com.colony.mod.registry;

import com.colony.mod.ColonyMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Deferred registration of all custom {@link Item}s for the Colony mod.
 */
public final class ColonyItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(BuiltInRegistries.ITEM, ColonyMod.MOD_ID);

    // Colony-specific items (e.g. blueprints, colony charter) registered here in future phases.

    private ColonyItems() {}
}
