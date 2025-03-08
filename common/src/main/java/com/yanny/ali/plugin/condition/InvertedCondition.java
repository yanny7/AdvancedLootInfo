package com.yanny.ali.plugin.condition;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootCondition;
import com.yanny.ali.plugin.TooltipUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.ali.plugin.TooltipUtils.pad;
import static com.yanny.ali.plugin.TooltipUtils.translatable;

public class InvertedCondition implements ILootCondition {
    public final ILootCondition term;

    public InvertedCondition(IContext context, LootItemCondition condition) {
        LootItemCondition termCondition = ((InvertedLootItemCondition) condition).term();
        term = context.utils().convertCondition(context, termCondition);
    }

    public InvertedCondition(IContext context, FriendlyByteBuf buf) {
        term = context.utils().decodeCondition(context, buf);
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        context.utils().encodeCondition(context, buf, term);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.condition.inverted")));
        components.addAll(term.getTooltip( pad + 1));

        return components;
    }
}
