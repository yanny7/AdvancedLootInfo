package com.yanny.ali.plugin.function;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.mixin.MixinIntRange;
import com.yanny.ali.mixin.MixinLimitCount;
import com.yanny.ali.plugin.FunctionTooltipUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.List;

public class LimitCountAliFunction extends LootConditionalAliFunction {
    public final RangeValue min;
    public final RangeValue max;

    public LimitCountAliFunction(IContext context, LootItemFunction function) {
        super(context, function);
        IntRange range = ((MixinLimitCount) function).getLimiter();
        min = context.utils().convertNumber(context, ((MixinIntRange) range).getMin());
        max = context.utils().convertNumber(context, ((MixinIntRange) range).getMax());
    }

    public LimitCountAliFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        min = new RangeValue(buf);
        max = new RangeValue(buf);
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        min.encode(buf);
        max.encode(buf);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        return FunctionTooltipUtils.getLimitCountTooltip(pad, min, max);
    }
}
