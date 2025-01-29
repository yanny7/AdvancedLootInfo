package com.yanny.advanced_loot_info.plugin.condition;

import com.yanny.advanced_loot_info.api.ILootCondition;
import com.yanny.advanced_loot_info.manager.PluginManager;
import com.yanny.advanced_loot_info.mixin.MixinCompositeLootItemCondition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.CompositeLootItemCondition;

import java.util.List;

public abstract class CompositeCondition implements ILootCondition {
    public final List<ILootCondition> terms;

    public CompositeCondition(LootContext lootContext, CompositeLootItemCondition condition) {
        terms = PluginManager.REGISTRY.convertConditions(lootContext, ((MixinCompositeLootItemCondition) condition).getTerms());
    }

    public CompositeCondition(FriendlyByteBuf buf) {
        terms = PluginManager.REGISTRY.decodeConditions(buf);
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        PluginManager.REGISTRY.encodeConditions(buf, terms);
    }
}
