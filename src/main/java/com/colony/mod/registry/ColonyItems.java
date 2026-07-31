package com.colony.mod.registry;

import com.colony.mod.ColonyMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

/**
 * Registration of all custom {@link Item}s for the Colony mod.
 */
public final class ColonyItems {

    /**
     * Colony Currency — the local monetary token used for wages, rent, and trade within
     * the colony. Players can withdraw this from the town treasury by presenting their
     * wallet balance at the TownLedgerBlock or through a future ATM-style block.
     *
     * <p>Stack size 64; max durability = 1 (it's a simple coin token, not a tool).
     */
    public static Item COLONY_CURRENCY;

    public static void register() {
        COLONY_CURRENCY = Registry.register(
                BuiltInRegistries.ITEM,
                ResourceLocation.fromNamespaceAndPath(ColonyMod.MOD_ID, "colony_currency"),
                new Item(new Item.Properties().stacksTo(64))
        );
    }

    private ColonyItems() {}
}
