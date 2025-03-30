package com.yanny.ali.plugin.condition;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootCondition;
import com.yanny.ali.mixin.MixinCanToolPerformAction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class CanToolPerformActionAliCondition implements ILootCondition {
    public final String action;

    public CanToolPerformActionAliCondition(IContext context, LootItemCondition condition) {
        action = ((MixinCanToolPerformAction) condition).getAction().name();
    }

    public CanToolPerformActionAliCondition(IContext context, FriendlyByteBuf buf) {
        action = buf.readUtf();
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        buf.writeUtf(action);
    }
}
