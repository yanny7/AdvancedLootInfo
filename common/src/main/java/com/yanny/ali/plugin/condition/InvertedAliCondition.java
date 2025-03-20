package com.yanny.ali.plugin.condition;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootCondition;
import com.yanny.ali.mixin.MixinInvertedLootItemCondition;
import com.yanny.ali.plugin.ConditionTooltipUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public class InvertedAliCondition implements ILootCondition {
    public final ILootCondition term;

    public InvertedAliCondition(IContext context, LootItemCondition condition) {
        LootItemCondition termCondition = ((MixinInvertedLootItemCondition) condition).getTerm();
        term = context.utils().convertCondition(context, termCondition);
    }

    public InvertedAliCondition(IContext context, FriendlyByteBuf buf) {
        term = context.utils().decodeCondition(context, buf);
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        context.utils().encodeCondition(context, buf, term);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        return ConditionTooltipUtils.getInvertedTooltip(pad, term);
    }
}
