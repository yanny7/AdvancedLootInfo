package com.yanny.emi_loot_addon.network.condition;

import com.yanny.emi_loot_addon.network.LootCondition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class EntityScoresCondition extends LootCondition {

    public EntityScoresCondition(LootContext lootContext, LootItemCondition condition) {
        super(ConditionType.of(condition.getType()));
    }

    public EntityScoresCondition(ConditionType type, FriendlyByteBuf buf) {
        super(type);
    }

    @Override
    public void encode(FriendlyByteBuf buf) {

    }
}
