package com.colony.mod.registry;

import com.colony.mod.ColonyMod;
import com.colony.mod.block.TownLedgerBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Registration of all custom blocks for the Colony mod.
 *
 * <p>Blocks here are "Smart Objects" — they advertise utility scores to nearby colonists.
 * Additional smart-object blocks (e.g. colony campfire, notice board) can be registered here.
 */
public final class ColonyBlocks {

    /**
     * The Town Ledger block — placed by the autonomous builder at the colony town centre.
     * Right-clicking opens the {@link com.colony.mod.client.TownLedgerScreen}.
     */
    public static TownLedgerBlock TOWN_LEDGER;

    public static void register() {
        TOWN_LEDGER = Registry.register(
                BuiltInRegistries.BLOCK,
                ResourceLocation.fromNamespaceAndPath(ColonyMod.MOD_ID, "town_ledger"),
                new TownLedgerBlock(BlockBehaviour.Properties.of()
                        .strength(2.5f)
                        .requiresCorrectToolForDrops())
        );
    }

    private ColonyBlocks() {}
}
