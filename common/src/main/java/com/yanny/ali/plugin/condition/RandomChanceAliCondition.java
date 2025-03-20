package com.yanny.ali.plugin.condition;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootCondition;
import com.yanny.ali.mixin.MixinLootItemRandomChanceCondition;
import com.yanny.ali.plugin.ConditionTooltipUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public class RandomChanceAliCondition implements ILootCondition {
    public final float probability;

    public RandomChanceAliCondition(IContext context, LootItemCondition condition) {
        probability = ((MixinLootItemRandomChanceCondition) condition).getProbability();
    }

    public RandomChanceAliCondition(IContext context, FriendlyByteBuf buf) {
        probability = buf.readFloat();
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        buf.writeFloat(probability);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        return ConditionTooltipUtils.getRandomChanceTooltip(pad, probability);
    }
}
