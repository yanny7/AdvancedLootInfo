package com.yanny.emi_loot_addon.network.condition;

import com.yanny.emi_loot_addon.mixin.MixinInvertedLootItemCondition;
import com.yanny.emi_loot_addon.network.LootCondition;
import com.yanny.emi_loot_addon.network.LootUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

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
        term = LootUtils.CONDITION_DECODE_MAP.get(termType).apply(type, buf);
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(termType);
        term.encode(buf);
    }
}
