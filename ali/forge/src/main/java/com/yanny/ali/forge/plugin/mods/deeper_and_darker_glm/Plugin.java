package com.yanny.ali.forge.plugin.mods.deeper_and_darker_glm;

import com.yanny.ali.api.AliEntrypoint;
import com.yanny.ali.forge.plugin.IForgePlugin;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.ali.plugin.glm.ILootTableIdConditionPredicate;
import org.jetbrains.annotations.NotNull;

@AliEntrypoint
public class Plugin implements IForgePlugin {
    @NotNull
    @Override
    public String getModId() {
        return "deeperdarker";
    }

    @Override
    public void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry, ILootTableIdConditionPredicate predicate) {
        GlobalLootModifierUtils.registerGlobalLootModifier(registry, AddItemModifier.class, predicate);
    }
}
