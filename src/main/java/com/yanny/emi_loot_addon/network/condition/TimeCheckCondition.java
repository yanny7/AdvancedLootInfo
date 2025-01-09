package com.yanny.emi_loot_addon.network.condition;

import com.yanny.emi_loot_addon.mixin.MixinIntRange;
import com.yanny.emi_loot_addon.mixin.MixinTimeCheck;
import com.yanny.emi_loot_addon.network.LootCondition;
import com.yanny.emi_loot_addon.network.RangeValue;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

import static com.yanny.emi_loot_addon.compatibility.EmiUtils.pad;
import static com.yanny.emi_loot_addon.compatibility.EmiUtils.translatable;

public class TimeCheckCondition extends LootCondition {
    @Nullable
    public final Long period;
    public final RangeValue min;
    public final RangeValue max;

    public TimeCheckCondition(LootContext lootContext, LootItemCondition condition) {
        super(ConditionType.of(condition.getType()));
        period = ((MixinTimeCheck) condition).getPeriod();
        min = RangeValue.of(lootContext, ((MixinIntRange) ((MixinTimeCheck) condition).getValue()).getMin());
        max = RangeValue.of(lootContext, ((MixinIntRange) ((MixinTimeCheck) condition).getValue()).getMax());
    }

    public TimeCheckCondition(ConditionType type, FriendlyByteBuf buf) {
        super(type);
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
        List<Component> components = super.getTooltip(pad);

        components.add(pad(pad + 1, translatable("emi.property.condition.time_check.period", period)));
        components.add(pad(pad + 1, translatable("emi.property.condition.time_check.value", RangeValue.rangeToString(min, max))));

        return components;
    }
}
