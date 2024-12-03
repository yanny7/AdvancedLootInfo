package com.yanny.emi_loot_addon.network.condition;

import com.yanny.emi_loot_addon.network.LootCondition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class TimeCheckCondition extends LootCondition {

    public TimeCheckCondition(LootContext lootContext, LootItemCondition condition) {
        super(ConditionType.of(condition.getType()));
    }

    public TimeCheckCondition(ConditionType type, FriendlyByteBuf buf) {
        super(type);
    }

    @Override
    public void encode(FriendlyByteBuf buf) {

    }
}
