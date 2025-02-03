package com.yanny.advanced_loot_info.plugin.condition;

import com.yanny.advanced_loot_info.api.IContext;
import com.yanny.advanced_loot_info.api.ILootCondition;
import com.yanny.advanced_loot_info.api.RangeValue;
import com.yanny.advanced_loot_info.mixin.MixinIntRange;
import com.yanny.advanced_loot_info.mixin.MixinValueCheckCondition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.advanced_loot_info.compatibility.EmiUtils.pad;
import static com.yanny.advanced_loot_info.compatibility.EmiUtils.translatable;

public class ValueCheckCondition implements ILootCondition {
    public final RangeValue provider;
    public final RangeValue min;
    public final RangeValue max;

    public ValueCheckCondition(IContext context, LootItemCondition condition) {
        provider = context.utils().convertNumber(context, ((MixinValueCheckCondition) condition).getProvider());
        min = context.utils().convertNumber(context, ((MixinIntRange) ((MixinValueCheckCondition) condition).getRange()).getMin());
        max = context.utils().convertNumber(context, ((MixinIntRange) ((MixinValueCheckCondition) condition).getRange()).getMax());
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

        components.add(pad(pad, translatable("emi.type.advanced_loot_info.condition.value_check")));
        components.add(pad(pad + 1, translatable("emi.property.condition.value_check.provider", provider)));
        components.add(pad(pad + 1, translatable("emi.property.condition.value_check.range", RangeValue.rangeToString(min, max))));

        return components;
    }
}
