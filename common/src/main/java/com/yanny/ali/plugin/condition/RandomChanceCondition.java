package com.yanny.ali.plugin.condition;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootCondition;
import com.yanny.ali.mixin.MixinLootItemRandomChanceCondition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.ali.plugin.TooltipUtils.pad;
import static com.yanny.ali.plugin.TooltipUtils.translatable;

public class RandomChanceCondition implements ILootCondition {
    public final float probability;

    public RandomChanceCondition(IContext context, LootItemCondition condition) {
        probability = ((MixinLootItemRandomChanceCondition) condition).getProbability();
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
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.condition.random_chance")));
        components.add(pad(pad + 1, translatable("ali.property.condition.random_chance.probability", probability)));

        return components;
    }
}
