package com.yanny.alicompat.compat.aether;

import com.aetherteam.aetherfabric.common.loot.LootModifier;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LootModifierAccessor extends BaseAccessor<LootModifier> {
    @FieldAccessor
    protected LootItemCondition[] conditions;

    public LootModifierAccessor(LootModifier parent) {
        super(parent);
    }

    public LootItemCondition[] getConditions() {
        return conditions;
    }
}
