package com.yanny.ali.plugin.lootjs.server;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.TooltipNode;
import com.yanny.ali.plugin.lootjs.modifier.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.*;

public class LootJsFunctionTooltipUtils {
    @NotNull
    public static ITooltipNode customPlayerTooltip(IServerUtils utils, CustomPlayerFunction function) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.custom_player"));

        tooltip.add(new TooltipNode(translatable("ali.property.value.detail_not_available")));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode dropExperienceTooltip(IServerUtils utils, DropExperienceFunction function) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.drop_experience"));

        tooltip.add(getIntegerTooltip(utils, "ali.property.value.amount", function.amount));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode explodeTooltip(IServerUtils utils, ExplodeFunction function) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.explode"));

        tooltip.add(getFloatTooltip(utils, "ali.property.value.radius", function.radius));
        tooltip.add(getBooleanTooltip(utils, "ali.property.value.fire", function.fire));
        tooltip.add(getEnumTooltip(utils, "ali.property.value.mode", function.mode));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode lightningStrikeTooltip(IServerUtils utils, LightningStrikeFunction function) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.lightning_strike"));

        tooltip.add(getBooleanTooltip(utils, "ali.property.value.should_damage_entity", function.shouldDamageEntity));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode modifiedItemTooltip(IServerUtils utils, ModifiedItemFunction function) {
        return new TooltipNode(Component.translatable("ali.type.function.modified_item").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
    }
}
