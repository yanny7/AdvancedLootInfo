package com.yanny.emi_loot_addon.network.condition;

import com.yanny.emi_loot_addon.network.TooltipUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.CompositeLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public class AllOfCondition extends CompositeCondition {
    public AllOfCondition(LootContext lootContext, LootItemCondition condition) {
        super(lootContext, (CompositeLootItemCondition) condition);
    }

    public AllOfCondition(ConditionType type, FriendlyByteBuf buf) {
        super(type, buf);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = super.getTooltip(pad);

        components.addAll(TooltipUtils.getConditions(terms, pad + 1));

        return components;
    }
}
