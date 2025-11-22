package com.yanny.ali.plugin.mods.deeper_and_darker_glm;

import com.yanny.ali.api.AliEntrypoint;
import com.yanny.ali.plugin.GlobalLootModifierUtils;
import com.yanny.ali.plugin.IForgePlugin;

@AliEntrypoint
public class Plugin implements IForgePlugin {
    @Override
    public String getModId() {
        return "deeperdarker";
    }

    @Override
    public void registerGLM(IRegistry registry) {
        GlobalLootModifierUtils.registerGlobalLootModifier(registry, AddItemModifier.class);
    }
}
