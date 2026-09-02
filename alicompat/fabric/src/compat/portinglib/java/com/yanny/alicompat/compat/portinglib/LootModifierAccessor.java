package com.yanny.alicompat.compat.portinglib;

import com.yanny.ali.plugin.mods.BaseAccessor;
import com.yanny.ali.plugin.mods.FieldAccessor;
import io.github.fabricators_of_create.porting_lib.loot.LootModifier;
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
