package com.yanny.emi_loot_addon.network.condition;

import com.yanny.emi_loot_addon.EmiLootMod;
import com.yanny.emi_loot_addon.mixin.MixinLootItemRandomChanceCondition;
import com.yanny.emi_loot_addon.network.LootCondition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.emi_loot_addon.compatibility.EmiUtils.pad;
import static com.yanny.emi_loot_addon.compatibility.EmiUtils.translatable;

public class RandomChanceCondition extends LootCondition {
    public final float probability;

    public RandomChanceCondition(LootContext lootContext, LootItemCondition condition) {
        super(ConditionType.of(condition.getType()));
        probability = ((MixinLootItemRandomChanceCondition) condition).getProbability();
    }

    public RandomChanceCondition(ConditionType type, FriendlyByteBuf buf) {
        super(type);
        probability = buf.readFloat();
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeFloat(probability);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        if (EmiLootMod.CONFIGURATION.isDebug()) {
            components.add(pad(pad, translatable("emi.debug.random_chance", probability)));
        }

        return components;
    }
}
