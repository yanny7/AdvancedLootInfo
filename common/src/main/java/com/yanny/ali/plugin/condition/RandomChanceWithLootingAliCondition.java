package com.yanny.ali.plugin.condition;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootCondition;
import com.yanny.ali.mixin.MixinLootItemRandomChanceWithLootingCondition;
import com.yanny.ali.plugin.ConditionTooltipUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public class RandomChanceWithLootingAliCondition implements ILootCondition {
    public final float percent;
    public final float multiplier;

    public RandomChanceWithLootingAliCondition(IContext context, LootItemCondition condition) {
        percent = ((MixinLootItemRandomChanceWithLootingCondition) condition).getPercent();
        multiplier = ((MixinLootItemRandomChanceWithLootingCondition) condition).getLootingMultiplier();
    }

    public RandomChanceWithLootingAliCondition(IContext context, FriendlyByteBuf buf) {
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
        return ConditionTooltipUtils.getRandomChanceWithLootingTooltip(pad, percent, multiplier);
    }
}
