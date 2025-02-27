package com.yanny.ali.plugin.condition;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootCondition;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.mixin.MixinIntRange;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.ali.plugin.TooltipUtils.pad;
import static com.yanny.ali.plugin.TooltipUtils.translatable;

public class ValueCheckCondition implements ILootCondition {
    public final RangeValue provider;
    public final RangeValue min;
    public final RangeValue max;

    public ValueCheckCondition(IContext context, LootItemCondition condition) {
        provider = context.utils().convertNumber(context, ((net.minecraft.world.level.storage.loot.predicates.ValueCheckCondition) condition).provider());
        min = context.utils().convertNumber(context, ((MixinIntRange) ((net.minecraft.world.level.storage.loot.predicates.ValueCheckCondition) condition).range()).getMin());
        max = context.utils().convertNumber(context, ((MixinIntRange) ((net.minecraft.world.level.storage.loot.predicates.ValueCheckCondition) condition).range()).getMax());
    }

    public ValueCheckCondition(IContext context, FriendlyByteBuf buf) {
        provider = new RangeValue(buf);
        min = new RangeValue(buf);
        max = new RangeValue(buf);
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        provider.encode(buf);
        min.encode(buf);
        max.encode(buf);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.condition.value_check")));
        components.add(pad(pad + 1, translatable("ali.property.condition.value_check.provider", provider)));
        components.add(pad(pad + 1, translatable("ali.property.condition.value_check.range", RangeValue.rangeToString(min, max))));

        return components;
    }
}
