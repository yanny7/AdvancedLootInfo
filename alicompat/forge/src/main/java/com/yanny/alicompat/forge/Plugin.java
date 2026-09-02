package com.yanny.alicompat.forge;

import com.yanny.ali.api.AliEntrypoint;
import com.yanny.ali.api.IClientRegistry;
import com.yanny.ali.api.ICommonRegistry;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.ali.plugin.glm.ILootTableIdConditionPredicate;
import com.yanny.alicompat.ModCompatManager;
import com.yanny.alicompat.Utils;
import org.jetbrains.annotations.NotNull;

@AliEntrypoint
public class Plugin implements IGlobalLootModifierPlugin {
    @NotNull
    @Override
    public String getModId() {
        return Utils.MOD_ID;
    }

    @Override
    public void registerCommon(ICommonRegistry registry) {
        ModCompatManager.registerCommon(registry);
    }

    @Override
    public void registerClient(IClientRegistry registry) {
        ModCompatManager.registerClient(registry);
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        ModCompatManager.registerServer(registry);
    }

    @Override
    public void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry, ILootTableIdConditionPredicate predicate) {
        ModCompatManager.registerGlobalLootModifier(registry, predicate);
    }
}
