package com.yanny.advanced_loot_info.plugin.condition;

import com.yanny.advanced_loot_info.api.IContext;
import com.yanny.advanced_loot_info.api.ILootCondition;
import com.yanny.advanced_loot_info.mixin.MixinLootItemRandomChanceWithLootingCondition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public class RandomChanceWithLootingCondition implements ILootCondition {
    public final float percent;
    public final float multiplier;

    public RandomChanceWithLootingCondition(IContext context, LootItemCondition condition) {
        percent = ((MixinLootItemRandomChanceWithLootingCondition) condition).getPercent();
        multiplier = ((MixinLootItemRandomChanceWithLootingCondition) condition).getLootingMultiplier();
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
        return List.of();
    }
}
