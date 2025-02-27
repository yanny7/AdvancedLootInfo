package com.yanny.ali.plugin.condition;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootCondition;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.mixin.MixinIntRange;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.TimeCheck;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static com.yanny.ali.plugin.TooltipUtils.pad;
import static com.yanny.ali.plugin.TooltipUtils.translatable;

public class TimeCheckCondition implements ILootCondition {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public final Optional<Long> period;
    public final RangeValue min;
    public final RangeValue max;

    public TimeCheckCondition(IContext context, LootItemCondition condition) {
        period = ((TimeCheck) condition).period();
        min = context.utils().convertNumber(context, ((MixinIntRange) ((TimeCheck) condition).value()).getMin());
        max = context.utils().convertNumber(context, ((MixinIntRange) ((TimeCheck) condition).value()).getMax());
    }

    public TimeCheckCondition(IContext context, FriendlyByteBuf buf) {
        period = buf.readOptional(FriendlyByteBuf::readLong);
        min = new RangeValue(buf);
        max = new RangeValue(buf);
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        buf.writeOptional(period, FriendlyByteBuf::writeLong);
        min.encode(buf);
        max.encode(buf);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.condition.time_check")));
        components.add(pad(pad + 1, translatable("ali.property.condition.time_check.period", period)));
        components.add(pad(pad + 1, translatable("ali.property.condition.time_check.value", RangeValue.rangeToString(min, max))));

        return components;
    }
}
