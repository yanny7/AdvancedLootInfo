package com.yanny.ali.plugin.condition;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootCondition;
import com.yanny.ali.mixin.MixinCompositeLootItemCondition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.loot.predicates.CompositeLootItemCondition;

import java.util.List;

public abstract class CompositeCondition implements ILootCondition {
    public final List<ILootCondition> terms;

    public CompositeCondition(IContext context, CompositeLootItemCondition condition) {
        terms = context.utils().convertConditions(context, ((MixinCompositeLootItemCondition) condition).getTerms());
    }

    public CompositeCondition(IContext context, FriendlyByteBuf buf) {
        terms = context.utils().decodeConditions(context, buf);
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        context.utils().encodeConditions(context, buf, terms);
    }
}
