package com.yanny.advanced_loot_info.plugin.function;

import com.yanny.advanced_loot_info.AdvancedLootInfoMod;
import com.yanny.advanced_loot_info.api.IContext;
import com.yanny.advanced_loot_info.api.RangeValue;
import com.yanny.advanced_loot_info.mixin.MixinIntRange;
import com.yanny.advanced_loot_info.mixin.MixinLimitCount;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.advanced_loot_info.compatibility.EmiUtils.pad;
import static com.yanny.advanced_loot_info.compatibility.EmiUtils.translatable;

public class LimitCountFunction extends LootConditionalFunction {
    public final RangeValue min;
    public final RangeValue max;

    public LimitCountFunction(IContext context, LootItemFunction function) {
        super(context, function);
        IntRange range = ((MixinLimitCount) function).getLimiter();
        min = context.registry().convertNumber(context, ((MixinIntRange) range).getMin());
        max = context.registry().convertNumber(context, ((MixinIntRange) range).getMax());
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

        if (AdvancedLootInfoMod.CONFIGURATION.isDebug()) {
            components.add(pad(pad, translatable("emi.debug.limit_count", min, max)));
        }

        return components;
    }
}
