package com.yanny.ali.plugin.condition;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootCondition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithLootingCondition;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.ali.plugin.TooltipUtils.pad;
import static com.yanny.ali.plugin.TooltipUtils.translatable;

public class RandomChanceWithLootingCondition implements ILootCondition {
    public final float percent;
    public final float multiplier;

    public RandomChanceWithLootingCondition(IContext context, LootItemCondition condition) {
        percent = ((LootItemRandomChanceWithLootingCondition) condition).percent();
        multiplier = ((LootItemRandomChanceWithLootingCondition) condition).lootingMultiplier();
    }

    public RandomChanceWithLootingCondition(IContext context, FriendlyByteBuf buf) {
        percent = buf.readFloat();
        multiplier = buf.readFloat();
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        buf.writeFloat(percent);
        buf.writeFloat(multiplier);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.condition.random_chance_with_looting")));
        components.add(pad(pad + 1, translatable("ali.property.condition.random_chance_with_looting.percent", percent)));
        components.add(pad(pad + 1, translatable("ali.property.condition.random_chance_with_looting.multiplier", multiplier)));

        return components;
    }
}
