package com.yanny.emi_loot_addon.network.function;

import com.yanny.emi_loot_addon.mixin.MixinSetNbtFunction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

public class SetNbtFunction extends LootConditionalFunction {
    public final String tag;

    public SetNbtFunction(LootContext lootContext, LootItemFunction function) {
        super(lootContext, function);
        tag = ((MixinSetNbtFunction) function).getTag().toString();
    }

    public SetNbtFunction(FunctionType type, FriendlyByteBuf buf) {
        super(type, buf);
        tag = buf.readUtf();
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        super.encode(buf);
        buf.writeUtf(tag);
    }
}
