package com.yanny.ali.plugin.condition;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootCondition;
import com.yanny.ali.mixin.MixinWeatherCheck;
import com.yanny.ali.plugin.ConditionTooltipUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.WeatherCheck;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class WeatherCheckAliCondition implements ILootCondition {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public final Optional<Boolean> isRaining;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public final Optional<Boolean> isThundering;

    public WeatherCheckAliCondition(IContext context, LootItemCondition condition) {
        isRaining = ((WeatherCheck) condition).isRaining();
        isThundering = ((WeatherCheck) condition).isThundering();
    }

    public WeatherCheckAliCondition(IContext context, FriendlyByteBuf buf) {
        isRaining = buf.readOptional(FriendlyByteBuf::readBoolean);
        isThundering = buf.readOptional(FriendlyByteBuf::readBoolean);
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        buf.writeOptional(isRaining, FriendlyByteBuf::writeBoolean);
        buf.writeOptional(isThundering, FriendlyByteBuf::writeBoolean);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        return ConditionTooltipUtils.getWeatherCheckTooltip(pad, isRaining, isThundering);
    }
}
