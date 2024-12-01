package com.yanny.emi_loot_addon.network.function;

import com.yanny.emi_loot_addon.mixin.MixinIntRange;
import com.yanny.emi_loot_addon.mixin.MixinLimitCount;
import com.yanny.emi_loot_addon.network.LootUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import org.jetbrains.annotations.NotNull;

public class LimitCountFunction extends LootConditionalFunction {
    public final int[] min;
    public final int[] max;

    public LimitCountFunction(LootContext lootContext, LootItemFunction function) {
        super(lootContext, function);
        IntRange range = ((MixinLimitCount) function).getLimiter();
        min = LootUtils.getInt(lootContext, ((MixinIntRange) range).getMin());
        max = LootUtils.getInt(lootContext, ((MixinIntRange) range).getMax());
    }

    public LimitCountFunction(FunctionType type, @NotNull FriendlyByteBuf buf) {
        super(type, buf);
        min = buf.readVarIntArray();
        max = buf.readVarIntArray();
    }

    @Override
    public void encode(@NotNull FriendlyByteBuf buf) {
        super.encode(buf);
        buf.writeVarIntArray(min);
        buf.writeVarIntArray(max);
    }
}
