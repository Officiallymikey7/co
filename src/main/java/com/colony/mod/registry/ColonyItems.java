package com.colony.mod.registry;

import com.colony.mod.ColonyMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Deferred registration of all custom {@link Item}s for the Colony mod.
 */
public final class ColonyItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(BuiltInRegistries.ITEM, ColonyMod.MOD_ID);

    /**
     * Colony Currency — the local monetary token used for wages, rent, and trade within
     * the colony. Players can withdraw this from the town treasury by presenting their
     * wallet balance at the TownLedgerBlock or through a future ATM-style block.
     *
     * <p>Stack size 64; max durability = 1 (it's a simple coin token, not a tool).
     */
    public static final DeferredHolder<Item, Item> COLONY_CURRENCY =
            ITEMS.register("colony_currency",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    private ColonyItems() {}
}
