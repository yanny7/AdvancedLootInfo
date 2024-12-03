package com.yanny.emi_loot_addon.network.condition;

import com.yanny.emi_loot_addon.mixin.MixinLootItemRandomChanceWithLootingCondition;
import com.yanny.emi_loot_addon.network.LootCondition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class RandomChanceWithLootingCondition extends LootCondition {
    public final float percent;
    public final float multiplier;

    public RandomChanceWithLootingCondition(LootContext lootContext, LootItemCondition condition) {
        super(ConditionType.of(condition.getType()));
        percent = ((MixinLootItemRandomChanceWithLootingCondition) condition).getPercent();
        multiplier = ((MixinLootItemRandomChanceWithLootingCondition) condition).getLootingMultiplier();
    }

    public RandomChanceWithLootingCondition(ConditionType type, FriendlyByteBuf buf) {
        super(type);
        percent = buf.readFloat();
        multiplier = buf.readFloat();
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeFloat(percent);
        buf.writeFloat(multiplier);
    }
}
