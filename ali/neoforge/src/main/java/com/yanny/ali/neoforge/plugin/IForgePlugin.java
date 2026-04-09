package com.yanny.ali.neoforge.plugin;

import com.yanny.ali.api.IPlugin;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.ali.plugin.glm.ILootTableIdConditionPredicate;

public interface IForgePlugin extends IPlugin {
    void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry, ILootTableIdConditionPredicate predicate);
}
