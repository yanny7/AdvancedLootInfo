package com.yanny.emi_loot_addon.network.function;

import com.yanny.emi_loot_addon.mixin.MixinLootingEnchantFunction;
import com.yanny.emi_loot_addon.network.value.RangeValue;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import org.jetbrains.annotations.NotNull;

public class LootingEnchantFunction extends LootConditionalFunction {
    public final RangeValue value;
    public final int limit;

    public LootingEnchantFunction(LootContext lootContext, LootItemFunction function) {
        super(lootContext, function);
        value = RangeValue.of(lootContext, ((MixinLootingEnchantFunction) function).getValue());
        limit = ((MixinLootingEnchantFunction) function).getLimit();
    }

    public LootingEnchantFunction(FunctionType type, @NotNull FriendlyByteBuf buf) {
        super(type, buf);
        value = new RangeValue(buf);
        limit = buf.readInt();
    }

    @Override
    public void encode(@NotNull FriendlyByteBuf buf) {
        super.encode(buf);
        value.encode(buf);
        buf.writeInt(limit);
    }
}
