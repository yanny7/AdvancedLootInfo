package com.yanny.ali.plugin.condition;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootCondition;
import com.yanny.ali.mixin.MixinCanToolPerformAction;
import com.yanny.ali.plugin.TooltipUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

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
        return List.of(TooltipUtils.pad(pad, TooltipUtils.translatable("ali.type.condition.loot_condition_type", TooltipUtils.value(action))));
    }
}
