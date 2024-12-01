package com.yanny.emi_loot_addon.network.function;

import com.yanny.emi_loot_addon.mixin.MixinLootItemConditionalFunction;
import com.yanny.emi_loot_addon.network.LootCondition;
import com.yanny.emi_loot_addon.network.LootFunction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LootConditionalFunction extends LootFunction {
    public final List<LootCondition> conditions;

    public LootConditionalFunction(LootContext context, LootItemFunction function) {
        super(FunctionType.of(function.getType()));
        conditions = LootCondition.of(context, ((MixinLootItemConditionalFunction) function).getPredicates());
    }

    public LootConditionalFunction(FunctionType type, @NotNull FriendlyByteBuf buf) {
        super(type);
        conditions = LootCondition.decode(buf);
    }

    @Override
    public void encode(@NotNull FriendlyByteBuf buf) {
        LootCondition.encode(buf, conditions);
    }
}
