package com.yanny.advanced_loot_info.plugin.condition;

import com.yanny.advanced_loot_info.api.IContext;
import com.yanny.advanced_loot_info.api.ILootCondition;
import com.yanny.advanced_loot_info.mixin.MixinInvertedLootItemCondition;
import com.yanny.advanced_loot_info.plugin.TooltipUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.advanced_loot_info.compatibility.EmiUtils.pad;
import static com.yanny.advanced_loot_info.compatibility.EmiUtils.translatable;

public class InvertedCondition implements ILootCondition {
    public final ILootCondition term;

    public InvertedCondition(IContext context, LootItemCondition condition) {
        LootItemCondition termCondition = ((MixinInvertedLootItemCondition) condition).getTerm();
        term = context.registry().convertCondition(context, termCondition);
    }

    public InvertedCondition(IContext context, FriendlyByteBuf buf) {
        term = context.registry().decodeCondition(context, buf);
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        context.registry().encodeCondition(context, buf, term);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("emi.type.advanced_loot_info.condition.inverted")));
        components.addAll(TooltipUtils.getConditions(List.of(term), pad + 1));

        return components;
    }
}
