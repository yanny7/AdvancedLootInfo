package com.yanny.ali.plugin.function;

import com.yanny.ali.api.IContext;
import com.yanny.ali.mixin.MixinSetNbtFunction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

public class SetNbtAliFunction extends LootConditionalAliFunction {
    public final String tag;

    public SetNbtAliFunction(IContext context, LootItemFunction function) {
        super(context, function);
        tag = ((MixinSetNbtFunction) function).getTag().toString();
    }

    public SetNbtAliFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        tag = buf.readUtf();
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeUtf(tag);
    }
}
