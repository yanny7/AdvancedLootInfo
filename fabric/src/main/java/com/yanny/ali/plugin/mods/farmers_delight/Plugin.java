package com.yanny.ali.plugin.mods.farmers_delight;

import com.yanny.ali.api.AliEntrypoint;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.plugin.mods.PluginUtils;
import com.yanny.ali.plugin.mods.porting_lib.loot.GlobalLootModifierUtils;
import com.yanny.ali.plugin.mods.porting_lib.loot.IGlobalLootModifierPlugin;

@AliEntrypoint
public class Plugin implements IGlobalLootModifierPlugin {
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
    public void registerGLM(IRegistry registry) {
        GlobalLootModifierUtils.registerGlobalLootModifier(registry, AddItemModifier.class);
        GlobalLootModifierUtils.registerGlobalLootModifier(registry, AddLootTableModifier.class);
        GlobalLootModifierUtils.registerGlobalLootModifier(registry, PastrySlicingModifier.class);
        GlobalLootModifierUtils.registerGlobalLootModifier(registry, ReplaceItemModifier.class);
    }
}
