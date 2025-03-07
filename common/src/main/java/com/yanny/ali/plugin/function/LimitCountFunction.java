package com.yanny.ali.plugin.function;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.mixin.MixinIntRange;
import com.yanny.ali.mixin.MixinLimitCount;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.ali.plugin.TooltipUtils.pad;
import static com.yanny.ali.plugin.TooltipUtils.translatable;

public class LimitCountFunction extends LootConditionalFunction {
    public final RangeValue min;
    public final RangeValue max;

    public LimitCountFunction(IContext context, LootItemFunction function) {
        super(context, function);
        IntRange range = ((MixinLimitCount) function).getLimiter();
        min = context.utils().convertNumber(context, ((MixinIntRange) range).getMin());
        max = context.utils().convertNumber(context, ((MixinIntRange) range).getMax());
    }

    public LimitCountFunction(IContext context, FriendlyByteBuf buf) {
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
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.limit_count")));
        components.add(pad(pad + 1, translatable("ali.property.function.limit_count.min", min)));
        components.add(pad(pad + 1, translatable("ali.property.function.limit_count.max", max)));

        return components;
    }
}
