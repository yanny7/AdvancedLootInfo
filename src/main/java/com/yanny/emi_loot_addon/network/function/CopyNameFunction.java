package com.yanny.emi_loot_addon.network.function;

import com.yanny.emi_loot_addon.mixin.MixinCopyNameFunction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

public class CopyNameFunction extends LootConditionalFunction {
    public final String source;

    public CopyNameFunction(LootContext lootContext, LootItemFunction function) {
        super(lootContext, function);
        source = ((MixinCopyNameFunction) function).getSource().name;
    }

    public CopyNameFunction(FunctionType type, FriendlyByteBuf buf) {
        super(type, buf);
        source = buf.readUtf();
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        super.encode(buf);
        buf.writeUtf(source);
    }
}
