package com.yanny.advanced_loot_info.plugin.condition;

import com.yanny.advanced_loot_info.api.ILootCondition;
import com.yanny.advanced_loot_info.mixin.MixinCanToolPerformAction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

import static com.yanny.advanced_loot_info.compatibility.EmiUtils.*;

public class CanToolPerformActionCondition implements ILootCondition {
    public final String action;

    public CanToolPerformActionCondition(LootContext lootContext, LootItemCondition condition) {
        action = ((MixinCanToolPerformAction) condition).getAction().name();
    }

    public CanToolPerformActionCondition(FriendlyByteBuf buf) {
        action = buf.readUtf();
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(action);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        return List.of(pad(pad, translatable("emi.type.advanced_loot_info.condition.loot_condition_type", value(action))));
    }
}
