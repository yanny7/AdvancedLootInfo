package com.yanny.advanced_loot_info.plugin.condition;

import com.yanny.advanced_loot_info.api.IContext;
import com.yanny.advanced_loot_info.api.ILootCondition;
import com.yanny.advanced_loot_info.mixin.MixinWeatherCheck;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static com.yanny.advanced_loot_info.compatibility.emi.EmiUtils.pad;
import static com.yanny.advanced_loot_info.compatibility.emi.EmiUtils.translatable;

public class WeatherCheckCondition implements ILootCondition {
    @Nullable
    public final Boolean isRaining;
    @Nullable
    public final Boolean isThundering;

    public WeatherCheckCondition(IContext context, LootItemCondition condition) {
        isRaining = ((MixinWeatherCheck) condition).isIsRaining();
        isThundering = ((MixinWeatherCheck) condition).isIsThundering();
    }

    public WeatherCheckCondition(IContext context, FriendlyByteBuf buf) {
        isRaining = buf.readOptional(FriendlyByteBuf::readBoolean).orElse(null);
        isThundering = buf.readOptional(FriendlyByteBuf::readBoolean).orElse(null);
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        buf.writeOptional(Optional.ofNullable(isRaining), FriendlyByteBuf::writeBoolean);
        buf.writeOptional(Optional.ofNullable(isThundering), FriendlyByteBuf::writeBoolean);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("emi.type.advanced_loot_info.condition.weather_check")));

        if (isRaining != null) {
            components.add(pad(pad + 1, translatable("emi.property.condition.weather_check.is_raining", isRaining)));
        }

        if (isThundering != null) {
            components.add(pad(pad + 1, translatable("emi.property.condition.weather_check.is_thundering", isThundering)));
        }

        return components;
    }
}
