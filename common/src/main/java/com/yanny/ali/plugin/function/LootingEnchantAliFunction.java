package com.yanny.ali.plugin.function;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.mixin.MixinLootingEnchantFunction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

public class LootingEnchantAliFunction extends LootConditionalAliFunction {
    public final RangeValue value;
    public final int limit;

    public LootingEnchantAliFunction(IContext context, LootItemFunction function) {
        super(context, function);
        value = context.utils().convertNumber(context, ((MixinLootingEnchantFunction) function).getValue());
        limit = ((MixinLootingEnchantFunction) function).getLimit();
    }

    public LootingEnchantAliFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        value = new RangeValue(buf);
        limit = buf.readInt();
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        value.encode(buf);
        buf.writeInt(limit);
    }
}
