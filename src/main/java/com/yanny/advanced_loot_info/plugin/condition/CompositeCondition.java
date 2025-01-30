package com.yanny.advanced_loot_info.plugin.condition;

import com.yanny.advanced_loot_info.api.IContext;
import com.yanny.advanced_loot_info.api.ILootCondition;
import com.yanny.advanced_loot_info.mixin.MixinCompositeLootItemCondition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.loot.predicates.CompositeLootItemCondition;

import java.util.List;

public abstract class CompositeCondition implements ILootCondition {
    public final List<ILootCondition> terms;

    public CompositeCondition(IContext context, CompositeLootItemCondition condition) {
        terms = context.registry().convertConditions(context, ((MixinCompositeLootItemCondition) condition).getTerms());
    }

    public CompositeCondition(IContext context, FriendlyByteBuf buf) {
        terms = context.registry().decodeConditions(context, buf);
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        context.registry().encodeConditions(context, buf, terms);
    }
}
