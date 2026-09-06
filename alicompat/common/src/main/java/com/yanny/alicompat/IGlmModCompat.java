package com.yanny.alicompat;

import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;

public interface IGlmModCompat extends IModCompat {
    void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry);
}
