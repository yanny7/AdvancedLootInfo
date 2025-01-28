package com.yanny.advanced_loot_info.plugin.function;

import com.yanny.advanced_loot_info.AdvancedLootInfoMod;
import com.yanny.advanced_loot_info.mixin.MixinIntRange;
import com.yanny.advanced_loot_info.mixin.MixinLimitCount;
import com.yanny.advanced_loot_info.network.RangeValue;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.advanced_loot_info.compatibility.EmiUtils.pad;
import static com.yanny.advanced_loot_info.compatibility.EmiUtils.translatable;

public class LimitCountFunction extends LootConditionalFunction {
    public final RangeValue min;
    public final RangeValue max;

    public LimitCountFunction(LootContext lootContext, LootItemFunction function) {
        super(lootContext, function);
        IntRange range = ((MixinLimitCount) function).getLimiter();
        min = RangeValue.of(lootContext, ((MixinIntRange) range).getMin());
        max = RangeValue.of(lootContext, ((MixinIntRange) range).getMax());
    }

    public LimitCountFunction(FriendlyByteBuf buf) {
        super(buf);
        min = new RangeValue(buf);
        max = new RangeValue(buf);
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        super.encode(buf);
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
