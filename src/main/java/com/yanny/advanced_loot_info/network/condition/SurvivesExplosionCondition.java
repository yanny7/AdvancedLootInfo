package com.yanny.advanced_loot_info.network.condition;

import com.yanny.advanced_loot_info.network.LootCondition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SurvivesExplosionCondition extends LootCondition {
    public SurvivesExplosionCondition(LootContext lootContext, LootItemCondition condition) {
        super(ConditionType.of(condition.getType()));
    }

    public SurvivesExplosionCondition(ConditionType type, FriendlyByteBuf buf) {
        super(type);
    }

    @Override
    public void encode(FriendlyByteBuf buf) {}
}
