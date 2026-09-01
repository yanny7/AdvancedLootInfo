package com.yanny.alicompat;

import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.ali.plugin.glm.ILootTableIdConditionPredicate;

public interface IGlmModCompat extends IModCompat {
    void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry, ILootTableIdConditionPredicate predicate);
}
