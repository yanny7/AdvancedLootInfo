package com.yanny.ali.plugin.condition;

import com.yanny.ali.api.IContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.predicates.CompositeLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.ali.plugin.TooltipUtils.pad;
import static com.yanny.ali.plugin.TooltipUtils.translatable;

public class AnyOfCondition extends CompositeCondition {
    public AnyOfCondition(IContext context, LootItemCondition condition) {
        super(context, (CompositeLootItemCondition) condition);
    }

    public AnyOfCondition(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.condition.any_of")));
        terms.forEach((condition) -> components.addAll(condition.getTooltip(pad + 1)));

        return components;
    }
}
