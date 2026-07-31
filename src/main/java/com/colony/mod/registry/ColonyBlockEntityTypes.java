package com.colony.mod.registry;

import com.colony.mod.ColonyMod;
import com.colony.mod.block.TownLedgerBlock;
import com.colony.mod.block.TownLedgerBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Deferred registration of all custom {@link BlockEntityType}s for the Colony mod.
 */
public final class ColonyBlockEntityTypes {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, ColonyMod.MOD_ID);

    /** Block entity for the {@link TownLedgerBlock}. */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TownLedgerBlockEntity>> TOWN_LEDGER =
            BLOCK_ENTITY_TYPES.register("town_ledger",
                    () -> BlockEntityType.Builder
                            .of(TownLedgerBlockEntity::new, ColonyBlocks.TOWN_LEDGER.get())
                            .build(null));

    private ColonyBlockEntityTypes() {}
}
