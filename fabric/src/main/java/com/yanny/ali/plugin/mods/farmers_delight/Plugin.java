package com.yanny.ali.plugin.mods.farmers_delight;

import com.yanny.ali.api.AliEntrypoint;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.ali.plugin.glm.ILootTableIdConditionPredicate;
import com.yanny.ali.plugin.mods.PluginUtils;
import com.yanny.ali.plugin.mods.porting_lib.loot.IGlmPlugin;

@AliEntrypoint
public class Plugin implements IGlmPlugin {
    @Override
    public String getModId() {
        return "farmersdelight";
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerFunctionTooltip(registry, CopySkilletFunction.class);
        PluginUtils.registerFunctionTooltip(registry, CopyMealFunction.class);
    }

    @Override
    public void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry, ILootTableIdConditionPredicate predicate) {
        GlobalLootModifierUtils.registerGlobalLootModifier(registry, AddItemModifier.class, predicate);
        GlobalLootModifierUtils.registerGlobalLootModifier(registry, AddLootTableModifier.class, predicate);
        GlobalLootModifierUtils.registerGlobalLootModifier(registry, PastrySlicingModifier.class, predicate);
        GlobalLootModifierUtils.registerGlobalLootModifier(registry, ReplaceItemModifier.class, predicate);
    }
}
