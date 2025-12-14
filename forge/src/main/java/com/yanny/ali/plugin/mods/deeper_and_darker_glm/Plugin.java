package com.yanny.ali.plugin.mods.deeper_and_darker_glm;

import com.yanny.ali.api.AliEntrypoint;
import com.yanny.ali.plugin.IForgePlugin;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.ali.plugin.glm.ILootTableIdConditionPredicate;

@AliEntrypoint
public class Plugin implements IForgePlugin {
    @Override
    public String getModId() {
        return "deeperdarker";
    }

    @Override
    public void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry, ILootTableIdConditionPredicate predicate) {
        GlobalLootModifierUtils.registerGlobalLootModifier(registry, AddItemModifier.class, predicate);
    }
}
