package com.yanny.emi_loot_addon.network.condition;

import com.yanny.emi_loot_addon.mixin.MixinWeatherCheck;
import com.yanny.emi_loot_addon.network.LootCondition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

import static com.yanny.emi_loot_addon.compatibility.EmiUtils.pad;
import static com.yanny.emi_loot_addon.compatibility.EmiUtils.translatable;

public class WeatherCheckCondition extends LootCondition {
    @Nullable
    public final Boolean isRaining;
    @Nullable
    public final Boolean isThundering;

    public WeatherCheckCondition(LootContext lootContext, LootItemCondition condition) {
        super(ConditionType.of(condition.getType()));
        isRaining = ((MixinWeatherCheck) condition).isIsRaining();
        isThundering = ((MixinWeatherCheck) condition).isIsThundering();
    }

    public WeatherCheckCondition(ConditionType type, FriendlyByteBuf buf) {
        super(type);
        isRaining = buf.readOptional(FriendlyByteBuf::readBoolean).orElse(null);
        isThundering = buf.readOptional(FriendlyByteBuf::readBoolean).orElse(null);
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeOptional(Optional.ofNullable(isRaining), FriendlyByteBuf::writeBoolean);
        buf.writeOptional(Optional.ofNullable(isThundering), FriendlyByteBuf::writeBoolean);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = super.getTooltip(pad);

        if (isRaining != null) {
            components.add(pad(pad + 1, translatable("emi.property.condition.weather_check.is_raining", isRaining)));
        }

        if (isThundering != null) {
            components.add(pad(pad + 1, translatable("emi.property.condition.weather_check.is_thundering", isThundering)));
        }

        return components;
    }
}
