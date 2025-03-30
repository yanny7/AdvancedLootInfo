package com.yanny.ali.plugin.condition;

import com.yanny.ali.api.IContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.loot.predicates.CompositeLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class AnyOfAliCondition extends CompositeAliCondition {
    public AnyOfAliCondition(IContext context, LootItemCondition condition) {
        super(context, (CompositeLootItemCondition) condition);
    }

    public AnyOfAliCondition(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
    }
}
