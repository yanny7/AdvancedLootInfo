package com.yanny.advanced_loot_info.plugin.condition;

import com.yanny.advanced_loot_info.AdvancedLootInfoMod;
import com.yanny.advanced_loot_info.api.IContext;
import com.yanny.advanced_loot_info.api.ILootCondition;
import com.yanny.advanced_loot_info.mixin.MixinLootItemRandomChanceCondition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.advanced_loot_info.compatibility.emi.EmiUtils.pad;
import static com.yanny.advanced_loot_info.compatibility.emi.EmiUtils.translatable;

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

        if (AdvancedLootInfoMod.CONFIGURATION.isDebug()) {
            components.add(pad(pad, translatable("emi.debug.random_chance", probability)));
        }

        return components;
    }
}
