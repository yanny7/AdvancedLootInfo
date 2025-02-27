package com.yanny.ali.plugin.condition;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootCondition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.WeatherCheck;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static com.yanny.ali.plugin.TooltipUtils.pad;
import static com.yanny.ali.plugin.TooltipUtils.translatable;

public class WeatherCheckCondition implements ILootCondition {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public final Optional<Boolean> isRaining;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public final Optional<Boolean> isThundering;

    public WeatherCheckCondition(IContext context, LootItemCondition condition) {
        isRaining = ((WeatherCheck) condition).isRaining();
        isThundering = ((WeatherCheck) condition).isThundering();
    }

    public WeatherCheckCondition(IContext context, FriendlyByteBuf buf) {
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
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.condition.weather_check")));

        isRaining.ifPresent((raining) -> components.add(pad(pad + 1, translatable("ali.property.condition.weather_check.is_raining", raining))));
        isThundering.ifPresent((thundering) -> components.add(pad(pad + 1, translatable("ali.property.condition.weather_check.is_thundering", thundering))));

        return components;
    }
}
