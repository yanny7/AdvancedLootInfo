package com.yanny.emi_loot_addon.network.condition;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.CompositeLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class AnyOfCondition extends CompositeCondition {
    public AnyOfCondition(LootContext lootContext, LootItemCondition condition) {
        super(lootContext, (CompositeLootItemCondition) condition);
    }

    public AnyOfCondition(ConditionType type, FriendlyByteBuf buf) {
        super(type, buf);
    }
}
