package com.yanny.emi_loot_addon.network.condition;

import com.yanny.emi_loot_addon.mixin.MixinCompositeLootItemCondition;
import com.yanny.emi_loot_addon.network.LootCondition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.CompositeLootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CompositeCondition extends LootCondition {
    public final List<LootCondition> terms;

    public CompositeCondition(LootContext lootContext, CompositeLootItemCondition condition) {
        super(ConditionType.of(condition.getType()));
        terms = LootCondition.of(lootContext, ((MixinCompositeLootItemCondition) condition).getTerms());
    }

    public CompositeCondition(ConditionType type, FriendlyByteBuf buf) {
        super(type);
        terms = LootCondition.decode(buf);
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        LootCondition.encode(buf, terms);
    }
}
