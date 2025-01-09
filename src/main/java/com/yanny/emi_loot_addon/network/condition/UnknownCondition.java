package com.yanny.emi_loot_addon.network.condition;

import com.yanny.emi_loot_addon.network.LootCondition;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class UnknownCondition extends LootCondition {
    public final ResourceLocation conditionType;

    public UnknownCondition(LootContext lootContext, LootItemCondition condition) {
        super(ConditionType.of(condition.getType()));
        conditionType = BuiltInRegistries.LOOT_CONDITION_TYPE.getKey(condition.getType());
    }

    public UnknownCondition(ConditionType type, FriendlyByteBuf buf) {
        super(type);
        conditionType = buf.readResourceLocation();
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(conditionType);
    }
}
