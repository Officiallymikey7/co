package com.colony.mod.block;

import com.colony.mod.registry.ColonyBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Block entity for the {@link TownLedgerBlock}.
 *
 * <p>Currently acts as a marker entity. Future phases may store the last-fetched ledger
 * snapshot here so the screen can display data without a server round-trip (e.g. for
 * spectators or offline viewing).
 */
public class TownLedgerBlockEntity extends BlockEntity {

    public TownLedgerBlockEntity(BlockPos pos, BlockState state) {
        super(ColonyBlockEntityTypes.TOWN_LEDGER.get(), pos, state);
    }
}
