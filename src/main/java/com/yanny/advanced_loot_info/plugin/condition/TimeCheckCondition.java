package com.yanny.advanced_loot_info.plugin.condition;

import com.yanny.advanced_loot_info.api.ILootCondition;
import com.yanny.advanced_loot_info.mixin.MixinIntRange;
import com.yanny.advanced_loot_info.mixin.MixinTimeCheck;
import com.yanny.advanced_loot_info.network.RangeValue;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static com.yanny.advanced_loot_info.compatibility.EmiUtils.pad;
import static com.yanny.advanced_loot_info.compatibility.EmiUtils.translatable;

public class TimeCheckCondition implements ILootCondition {
    @Nullable
    public final Long period;
    public final RangeValue min;
    public final RangeValue max;

    public TimeCheckCondition(LootContext lootContext, LootItemCondition condition) {
        period = ((MixinTimeCheck) condition).getPeriod();
        min = RangeValue.of(lootContext, ((MixinIntRange) ((MixinTimeCheck) condition).getValue()).getMin());
        max = RangeValue.of(lootContext, ((MixinIntRange) ((MixinTimeCheck) condition).getValue()).getMax());
    }

    public TimeCheckCondition(FriendlyByteBuf buf) {
        period = buf.readOptional(FriendlyByteBuf::readLong).orElse(null);
        min = new RangeValue(buf);
        max = new RangeValue(buf);
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeOptional(Optional.ofNullable(period), FriendlyByteBuf::writeLong);
        min.encode(buf);
        max.encode(buf);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("emi.type.advanced_loot_info.condition.time_check")));
        components.add(pad(pad + 1, translatable("emi.property.condition.time_check.period", period)));
        components.add(pad(pad + 1, translatable("emi.property.condition.time_check.value", RangeValue.rangeToString(min, max))));

        return components;
    }
}
