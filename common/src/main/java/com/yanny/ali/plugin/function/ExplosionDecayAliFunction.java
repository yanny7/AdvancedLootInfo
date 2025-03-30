package com.yanny.ali.plugin.function;

import com.yanny.ali.api.IContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

public class ExplosionDecayAliFunction extends LootConditionalAliFunction {
    public ExplosionDecayAliFunction(IContext context, LootItemFunction function) {
        super(context, function);
    }

    public ExplosionDecayAliFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
    }
}
