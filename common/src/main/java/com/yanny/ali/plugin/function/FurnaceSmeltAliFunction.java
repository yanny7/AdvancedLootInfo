package com.yanny.ali.plugin.function;

import com.yanny.ali.api.IContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

public class FurnaceSmeltAliFunction extends LootConditionalAliFunction {
    public FurnaceSmeltAliFunction(IContext context, LootItemFunction function) {
        super(context, function);
    }

    public FurnaceSmeltAliFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
    }
}
