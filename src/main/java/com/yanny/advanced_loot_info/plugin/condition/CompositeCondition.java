package com.yanny.advanced_loot_info.plugin.condition;

import com.yanny.advanced_loot_info.api.ILootCondition;
import com.yanny.advanced_loot_info.mixin.MixinCompositeLootItemCondition;
import com.yanny.advanced_loot_info.network.LootUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.CompositeLootItemCondition;

import java.util.List;

public abstract class CompositeCondition implements ILootCondition {
    public final List<ILootCondition> terms;

    public CompositeCondition(LootContext lootContext, CompositeLootItemCondition condition) {
        terms = LootUtils.ofCondition(lootContext, ((MixinCompositeLootItemCondition) condition).getTerms());
    }

    public CompositeCondition(FriendlyByteBuf buf) {
        terms = LootUtils.decodeCondition(buf);
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        LootUtils.encodeCondition(buf, terms);
    }
}
