package com.yanny.ali.plugin.condition;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootCondition;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.mixin.MixinIntRange;
import com.yanny.ali.mixin.MixinTimeCheck;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import javax.annotation.Nullable;
import java.util.Optional;

public class TimeCheckAliCondition implements ILootCondition {
    @Nullable
    public final Long period;
    public final RangeValue min;
    public final RangeValue max;

    public TimeCheckAliCondition(IContext context, LootItemCondition condition) {
        period = ((MixinTimeCheck) condition).getPeriod();
        min = context.utils().convertNumber(context, ((MixinIntRange) ((MixinTimeCheck) condition).getValue()).getMin());
        max = context.utils().convertNumber(context, ((MixinIntRange) ((MixinTimeCheck) condition).getValue()).getMax());
    }

    public TimeCheckAliCondition(IContext context, FriendlyByteBuf buf) {
        period = buf.readOptional(FriendlyByteBuf::readLong).orElse(null);
        min = new RangeValue(buf);
        max = new RangeValue(buf);
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        buf.writeOptional(Optional.ofNullable(period), FriendlyByteBuf::writeLong);
        min.encode(buf);
        max.encode(buf);
    }
}
