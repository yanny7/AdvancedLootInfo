package com.yanny.ali.plugin.mods.porting_lib.loot;

import com.yanny.ali.plugin.mods.BaseAccessor;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.FieldAccessor;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

@ClassAccessor("io.github.fabricators_of_create.porting_lib.loot.LootModifier")
public class LootModifier extends BaseAccessor<Object> {
    @FieldAccessor
    protected LootItemCondition[] conditions;

    public LootModifier(Object parent) {
        super(parent);
    }

    public LootItemCondition[] getConditions() {
        return conditions;
    }
}
