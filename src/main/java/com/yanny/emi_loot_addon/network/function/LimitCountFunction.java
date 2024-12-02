package com.yanny.emi_loot_addon.network.function;

import com.yanny.emi_loot_addon.mixin.MixinIntRange;
import com.yanny.emi_loot_addon.mixin.MixinLimitCount;
import com.yanny.emi_loot_addon.network.value.RangeValue;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import org.jetbrains.annotations.NotNull;

public class LimitCountFunction extends LootConditionalFunction {
    public final RangeValue min;
    public final RangeValue max;

    public LimitCountFunction(LootContext lootContext, LootItemFunction function) {
        super(lootContext, function);
        IntRange range = ((MixinLimitCount) function).getLimiter();
        min = RangeValue.of(lootContext, ((MixinIntRange) range).getMin());
        max = RangeValue.of(lootContext, ((MixinIntRange) range).getMax());
    }

    public LimitCountFunction(FunctionType type, @NotNull FriendlyByteBuf buf) {
        super(type, buf);
        min = new RangeValue(buf);
        max = new RangeValue(buf);
    }

    @Override
    public void encode(@NotNull FriendlyByteBuf buf) {
        super.encode(buf);
        min.encode(buf);
        max.encode(buf);
    }
}
