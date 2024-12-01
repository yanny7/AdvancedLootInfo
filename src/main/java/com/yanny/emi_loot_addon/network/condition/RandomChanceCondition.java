package com.yanny.emi_loot_addon.network.condition;

import com.yanny.emi_loot_addon.network.LootCondition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

public class RandomChanceCondition extends LootCondition {

    public RandomChanceCondition(LootContext lootContext, LootItemCondition condition) {
        super(ConditionType.of(condition.getType()));
    }

    public RandomChanceCondition(ConditionType type, @NotNull FriendlyByteBuf buf) {
        super(type);
    }

    @Override
    public void encode(@NotNull FriendlyByteBuf buf) {

    }
}
