package com.yanny.advanced_loot_info.network.condition;

import com.yanny.advanced_loot_info.mixin.MixinInvertedLootItemCondition;
import com.yanny.advanced_loot_info.network.LootCondition;
import com.yanny.advanced_loot_info.network.LootUtils;
import com.yanny.advanced_loot_info.network.TooltipUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public class InvertedCondition extends LootCondition {
    public final ConditionType termType;
    public final LootCondition term;

    public InvertedCondition(LootContext lootContext, LootItemCondition condition) {
        super(ConditionType.of(condition.getType()));
        LootItemCondition termCondition = ((MixinInvertedLootItemCondition) condition).getTerm();
        termType = ConditionType.of(termCondition.getType());
        term = LootUtils.CONDITION_MAP.get(termType).apply(lootContext, termCondition);
    }

    public InvertedCondition(ConditionType type, FriendlyByteBuf buf) {
        super(type);
        termType = buf.readEnum(ConditionType.class);
        term = LootUtils.CONDITION_DECODE_MAP.get(termType).apply(termType, buf);
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(termType);
        term.encode(buf);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = super.getTooltip(pad);

        components.addAll(TooltipUtils.getConditions(List.of(term), pad + 1));

        return components;
    }
}
