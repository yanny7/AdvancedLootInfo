package com.yanny.ali.plugin.condition;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootCondition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;

import java.util.List;

public class RandomChanceCondition implements ILootCondition {
    public final float probability;

    public RandomChanceCondition(IContext context, LootItemCondition condition) {
        probability = ((LootItemRandomChanceCondition) condition).probability();
    }

    public RandomChanceCondition(IContext context, FriendlyByteBuf buf) {
        probability = buf.readFloat();
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        buf.writeFloat(probability);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        return List.of();
    }
}
