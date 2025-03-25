package com.yanny.ali.plugin.condition;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootCondition;
import com.yanny.ali.mixin.MixinCanToolPerformAction;
import com.yanny.ali.plugin.GenericTooltipUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;
import java.util.Optional;

public class CanToolPerformActionCondition implements ILootCondition {
    public final String action;

    public CanToolPerformActionCondition(IContext context, LootItemCondition condition) {
        action = ((MixinCanToolPerformAction) condition).getAction().name();
    }

    public CanToolPerformActionCondition(IContext context, FriendlyByteBuf buf) {
        action = buf.readUtf();
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        buf.writeUtf(action);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        return GenericTooltipUtils.getStringTooltip(pad, "ali.type.condition.can_tool_perform_action", Optional.ofNullable(action));
    }
}
