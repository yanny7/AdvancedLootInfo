package com.yanny.ali.plugin.condition;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootCondition;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.mixin.MixinIntRange;
import com.yanny.ali.mixin.MixinValueCheckCondition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class ValueCheckAliCondition implements ILootCondition {
    public final RangeValue provider;
    public final RangeValue min;
    public final RangeValue max;

    public ValueCheckAliCondition(IContext context, LootItemCondition condition) {
        provider = context.utils().convertNumber(context, ((MixinValueCheckCondition) condition).getProvider());
        min = context.utils().convertNumber(context, ((MixinIntRange) ((MixinValueCheckCondition) condition).getRange()).getMin());
        max = context.utils().convertNumber(context, ((MixinIntRange) ((MixinValueCheckCondition) condition).getRange()).getMax());
    }

    public ValueCheckAliCondition(IContext context, FriendlyByteBuf buf) {
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
}
