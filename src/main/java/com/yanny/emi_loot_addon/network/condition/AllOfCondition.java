package com.yanny.emi_loot_addon.network.condition;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.CompositeLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class AllOfCondition extends CompositeCondition {
    public AllOfCondition(LootContext lootContext, LootItemCondition condition) {
        super(lootContext, (CompositeLootItemCondition) condition);
    }

    public AllOfCondition(ConditionType type, FriendlyByteBuf buf) {
        super(type, buf);
    }
}
