package com.yanny.advanced_loot_info.network.condition;

import com.yanny.advanced_loot_info.mixin.MixinIntRange;
import com.yanny.advanced_loot_info.mixin.MixinValueCheckCondition;
import com.yanny.advanced_loot_info.network.LootCondition;
import com.yanny.advanced_loot_info.network.RangeValue;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

import static com.yanny.advanced_loot_info.compatibility.EmiUtils.pad;
import static com.yanny.advanced_loot_info.compatibility.EmiUtils.translatable;

public class ValueCheckCondition extends LootCondition {
    public final RangeValue provider;
    public final RangeValue min;
    public final RangeValue max;

    public ValueCheckCondition(LootContext lootContext, LootItemCondition condition) {
        super(ConditionType.of(condition.getType()));
        provider = RangeValue.of(lootContext, ((MixinValueCheckCondition) condition).getProvider());
        min = RangeValue.of(lootContext, ((MixinIntRange) ((MixinValueCheckCondition) condition).getRange()).getMin());
        max = RangeValue.of(lootContext, ((MixinIntRange) ((MixinValueCheckCondition) condition).getRange()).getMax());
    }

    public ValueCheckCondition(ConditionType type, FriendlyByteBuf buf) {
        super(type);
        provider = new RangeValue(buf);
        min = new RangeValue(buf);
        max = new RangeValue(buf);
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        provider.encode(buf);
        min.encode(buf);
        max.encode(buf);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = super.getTooltip(pad);

        components.add(pad(pad + 1, translatable("emi.property.condition.value_check.provider", provider)));
        components.add(pad(pad + 1, translatable("emi.property.condition.value_check.range", RangeValue.rangeToString(min, max))));

        return components;
    }
}
