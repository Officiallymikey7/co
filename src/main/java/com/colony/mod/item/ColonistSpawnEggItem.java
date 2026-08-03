package com.colony.mod.item;

import com.colony.mod.entity.ColonistVariant;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ColonistSpawnEggItem extends SpawnEggItem {
    public ColonistSpawnEggItem(net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.Mob> type,
                                int backgroundColor,
                                int highlightColor,
                                Item.Properties properties) {
        super(type, backgroundColor, highlightColor, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("item.colony.colonist_spawn_egg.tooltip")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.colony.colonist_spawn_egg.variants", ColonistVariant.allIdsCsv())
                .withStyle(ChatFormatting.DARK_GRAY));
        CustomData customData = stack.getOrDefault(net.minecraft.core.component.DataComponents.ENTITY_DATA, CustomData.EMPTY);
        String selectedVariant = customData.copyTag().getString("variant");
        if (!selectedVariant.isBlank()) {
            tooltip.add(Component.translatable(
                    "item.colony.colonist_spawn_egg.selected_variant",
                    ColonistVariant.fromId(selectedVariant).id())
                    .withStyle(ChatFormatting.AQUA));
        }
    }
}
