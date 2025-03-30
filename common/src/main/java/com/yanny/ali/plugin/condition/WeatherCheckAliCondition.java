package com.yanny.ali.plugin.condition;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootCondition;
import com.yanny.ali.mixin.MixinWeatherCheck;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import javax.annotation.Nullable;
import java.util.Optional;

public class WeatherCheckAliCondition implements ILootCondition {
    @Nullable
    public final Boolean isRaining;
    @Nullable
    public final Boolean isThundering;

    public WeatherCheckAliCondition(IContext context, LootItemCondition condition) {
        isRaining = ((MixinWeatherCheck) condition).isIsRaining();
        isThundering = ((MixinWeatherCheck) condition).isIsThundering();
    }

    public WeatherCheckAliCondition(IContext context, FriendlyByteBuf buf) {
        isRaining = buf.readOptional(FriendlyByteBuf::readBoolean).orElse(null);
        isThundering = buf.readOptional(FriendlyByteBuf::readBoolean).orElse(null);
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        buf.writeOptional(Optional.ofNullable(isRaining), FriendlyByteBuf::writeBoolean);
        buf.writeOptional(Optional.ofNullable(isThundering), FriendlyByteBuf::writeBoolean);
    }
}
