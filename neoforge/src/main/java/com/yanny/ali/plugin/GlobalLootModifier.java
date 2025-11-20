package com.yanny.ali.plugin;

import com.yanny.ali.mixin.MixinLootModifier;
import com.yanny.ali.plugin.mods.BaseAccessor;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.LootModifier;

public abstract class GlobalLootModifier extends BaseAccessor<LootModifier> {
    protected final LootItemCondition[] conditions;

    public GlobalLootModifier(LootModifier parent) {
        super(parent);
        conditions = ((MixinLootModifier) parent).getConditions();
    }
}
