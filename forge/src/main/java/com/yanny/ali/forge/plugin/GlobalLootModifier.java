package com.yanny.ali.forge.plugin;

import com.yanny.ali.forge.mixin.MixinLootModifier;
import com.yanny.ali.plugin.mods.BaseAccessor;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.LootModifier;

public abstract class GlobalLootModifier extends BaseAccessor<LootModifier> {
    protected final LootItemCondition[] conditions;

    public GlobalLootModifier(LootModifier parent) {
        super(parent);
        conditions = ((MixinLootModifier) parent).getConditions();
    }
}
