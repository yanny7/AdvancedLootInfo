package com.yanny.ali.plugin.condition;

import com.yanny.ali.api.IContext;
import com.yanny.ali.plugin.ConditionTooltipUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.predicates.CompositeLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public class AllOfAliCondition extends CompositeAliCondition {
    public AllOfAliCondition(IContext context, LootItemCondition condition) {
        super(context, (CompositeLootItemCondition) condition);
    }

    public AllOfAliCondition(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        return ConditionTooltipUtils.getAllOfTooltip(pad, terms);
    }
}
