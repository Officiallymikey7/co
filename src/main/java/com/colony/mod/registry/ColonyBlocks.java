package com.colony.mod.registry;

import com.colony.mod.ColonyMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
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

    // Smart-object blocks will be registered here in future phases.
    // Example:
    // public static final var COLONY_BED = BLOCKS.register("colony_bed",
    //         () -> new ColonyBedBlock(BlockBehaviour.Properties.of()));

    private ColonyBlocks() {}
}
