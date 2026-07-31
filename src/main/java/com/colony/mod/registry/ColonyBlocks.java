package com.colony.mod.registry;

import com.colony.mod.ColonyMod;
import com.colony.mod.block.TownLedgerBlock;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Deferred registration of all custom {@link Block}s for the Colony mod.
 *
 * <p>Blocks here are "Smart Objects" — they advertise utility scores to nearby colonists.
 * Additional smart-object blocks (e.g. colony campfire, notice board) can be registered here.
 */
public final class ColonyBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(BuiltInRegistries.BLOCK, ColonyMod.MOD_ID);

    /**
     * The Town Ledger block — placed by the autonomous builder at the colony town centre.
     * Right-clicking opens the {@link com.colony.mod.client.TownLedgerScreen}.
     */
    public static final DeferredHolder<Block, TownLedgerBlock> TOWN_LEDGER =
            BLOCKS.register("town_ledger",
                    () -> new TownLedgerBlock(BlockBehaviour.Properties.of()
                            .strength(2.5f)
                            .requiresCorrectToolForDrops()));

    private ColonyBlocks() {}
}
