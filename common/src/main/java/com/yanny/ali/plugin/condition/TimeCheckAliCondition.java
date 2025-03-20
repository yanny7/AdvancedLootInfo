package com.yanny.ali.plugin.condition;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootCondition;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.mixin.MixinIntRange;
import com.yanny.ali.mixin.MixinTimeCheck;
import com.yanny.ali.plugin.ConditionTooltipUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.TimeCheck;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class TimeCheckAliCondition implements ILootCondition {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public final Optional<Long> period;
    public final RangeValue min;
    public final RangeValue max;

    public TimeCheckAliCondition(IContext context, LootItemCondition condition) {
        period = ((TimeCheck) condition).period();
        min = context.utils().convertNumber(context, ((MixinIntRange) ((TimeCheck) condition).value()).getMin());
        max = context.utils().convertNumber(context, ((MixinIntRange) ((TimeCheck) condition).value()).getMax());
    }

    public TimeCheckAliCondition(IContext context, FriendlyByteBuf buf) {
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
        return ConditionTooltipUtils.getTimeCheckTooltip(pad, period, min, max);
    }
}
