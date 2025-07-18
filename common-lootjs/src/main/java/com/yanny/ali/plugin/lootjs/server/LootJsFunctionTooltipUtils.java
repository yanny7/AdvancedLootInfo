package com.yanny.ali.plugin.lootjs.server;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.TooltipNode;
import com.yanny.ali.plugin.lootjs.modifier.CustomPlayerFunction;
import com.yanny.ali.plugin.lootjs.modifier.ModifiedItemFunction;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.translatable;

public class LootJsFunctionTooltipUtils {
    @NotNull
    public static ITooltipNode customPlayerTooltip(IServerUtils utils, CustomPlayerFunction function) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.custom_player"));

        tooltip.add(new TooltipNode(translatable("ali.property.value.detail_not_available")));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode modifiedItemTooltip(IServerUtils utils, ModifiedItemFunction function) {
        return new TooltipNode(Component.translatable("ali.type.function.modified_item").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
    }
}
