package com.colony.mod.registry;

import com.colony.mod.ColonyMod;
import com.colony.mod.block.TownLedgerBlockEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * Registration of all custom {@link BlockEntityType}s for the Colony mod.
 */
public final class ColonyBlockEntityTypes {

    /** Block entity for the {@link com.colony.mod.block.TownLedgerBlock}. */
    public static BlockEntityType<TownLedgerBlockEntity> TOWN_LEDGER;

    public static void register() {
        TOWN_LEDGER = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath(ColonyMod.MOD_ID, "town_ledger"),
                BlockEntityType.Builder
                        .of(TownLedgerBlockEntity::new, ColonyBlocks.TOWN_LEDGER)
                        .build(null)
        );
    }

    private ColonyBlockEntityTypes() {}
}
