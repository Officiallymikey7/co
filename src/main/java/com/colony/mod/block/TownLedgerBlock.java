package com.colony.mod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;

/**
 * The Town Ledger Block — a physical block placed by the colony builder at the town centre.
 *
 * <p>Right-clicking this block opens the {@link com.colony.mod.client.TownLedgerScreen} which
 * displays the colony's current status, economy, active projects, and crime log.
 *
 * <p>On open, the server sends a fresh {@link com.colony.mod.network.TownLedgerResponsePacket}
 * directly to the player so the screen reflects current state.
 */
public class TownLedgerBlock extends BaseEntityBlock {

    public TownLedgerBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TownLedgerBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            // Request fresh ledger data from the server; the response packet opens the screen.
            PacketDistributor.sendToPlayer(serverPlayer,
                    com.colony.mod.network.TownLedgerResponsePacket.from(
                            com.colony.mod.town.TownManager.get(serverPlayer.serverLevel())));
        }
        return InteractionResult.SUCCESS;
    }
}
