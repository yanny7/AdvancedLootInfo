package com.yanny.emi_loot_addon.network.condition;

import com.yanny.emi_loot_addon.mixin.MixinCanToolPerformAction;
import com.yanny.emi_loot_addon.network.LootCondition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

import static com.yanny.emi_loot_addon.compatibility.EmiUtils.*;

public class CanToolPerformActionCondition extends LootCondition {
    public final String action;

    public CanToolPerformActionCondition(LootContext lootContext, LootItemCondition condition) {
        super(ConditionType.of(condition.getType()));
        action = ((MixinCanToolPerformAction) condition).getAction().name();
    }

    public CanToolPerformActionCondition(ConditionType type, FriendlyByteBuf buf) {
        super(type);
        action = buf.readUtf();
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(action);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        return List.of(pad(pad, translatableType("emi.type.emi_loot_addon.condition", type, value(action))));
    }
}
