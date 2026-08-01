package com.colony.mod.registry;

import com.colony.mod.ColonyMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

/**
 * Registration of all custom {@link Item}s for the Colony mod.
 */
public final class ColonyItems {

    /**
     * Colony Currency — the local monetary token used for wages, rent, and trade within
     * the colony. Players can withdraw this from the town treasury by presenting their
     * wallet balance at the TownLedgerBlock or through a future ATM-style block.
     *
     * <p>Stack size 64; it's a simple coin token.
     */
    public static Item COLONY_CURRENCY;

    /**
     * Spawn egg for the {@link com.colony.mod.entity.ColonistEntity}.
     * Allows players and data-packs to spawn colonists via /give or creative inventory.
     * Must be registered after {@link ColonyEntityTypes#COLONIST} is set.
     */
    public static SpawnEggItem COLONIST_SPAWN_EGG;

    public static void register() {
        COLONY_CURRENCY = Registry.register(
                BuiltInRegistries.ITEM,
                ResourceLocation.fromNamespaceAndPath(ColonyMod.MOD_ID, "colony_currency"),
                new Item(new Item.Properties().stacksTo(64))
        );

        // Primary colour: warm brown (#8B6914); secondary: wheat (#F5DEB3)
        COLONIST_SPAWN_EGG = Registry.register(
                BuiltInRegistries.ITEM,
                ResourceLocation.fromNamespaceAndPath(ColonyMod.MOD_ID, "colonist_spawn_egg"),
                new SpawnEggItem(ColonyEntityTypes.COLONIST, 0x8B6914, 0xF5DEB3,
                        new Item.Properties())
        );
    }

    private ColonyItems() {}
}
