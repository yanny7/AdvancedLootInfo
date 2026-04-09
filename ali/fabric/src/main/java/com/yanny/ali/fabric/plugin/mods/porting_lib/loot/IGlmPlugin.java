package com.yanny.ali.fabric.plugin.mods.porting_lib.loot;

import com.yanny.ali.api.IPlugin;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.ali.plugin.glm.ILootTableIdConditionPredicate;

public interface IGlmPlugin extends IPlugin {
    void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry, ILootTableIdConditionPredicate predicate);
}
