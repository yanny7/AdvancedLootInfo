package com.yanny.alicompat.compat.aether;

import com.aetherteam.aether.loot.conditions.ConfigEnabled;
import com.aetherteam.aether.loot.functions.DoubleDrops;
import com.aetherteam.aether.loot.functions.SpawnTNT;
import com.aetherteam.aether.loot.functions.SpawnXP;
import com.aetherteam.aether.loot.functions.WhirlwindSpawnEntity;
import com.aetherteam.aether.loot.modifiers.*;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.alicompat.IGlmModCompat;
import com.yanny.alicompat.accessor.GlmAccessorUtils;
import com.yanny.alicompat.accessor.PluginUtils;
import org.jetbrains.annotations.NotNull;

public class AetherCompat implements IGlmModCompat {
    @NotNull
    @Override
    public String targetModId() {
        return AetherLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerConditionTooltip(registry, ConfigEnabled.class, ConfigEnabledAccessor.class);

        PluginUtils.registerFunctionTooltip(registry, DoubleDrops.class, DoubleDropsAccessor::new);
        PluginUtils.registerFunctionTooltip(registry, SpawnTNT.class, SpawnTNTAccessor::new);
        PluginUtils.registerFunctionTooltip(registry, SpawnXP.class, SpawnXPAccessor::new);
        PluginUtils.registerFunctionTooltip(registry, WhirlwindSpawnEntity.class, WhirlwindSpawnEntityAccessor.class);
    }

    @Override
    public void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry) {
        GlmAccessorUtils.registerGlobalLootModifier(registry, DoubleDropsModifier.class, DoubleDropsModifierAccessor.class);
        GlmAccessorUtils.registerGlobalLootModifier(registry, EnchantedGrassModifier.class, EnchantedGrassModifierAccessor.class);
        GlmAccessorUtils.registerGlobalLootModifier(registry, GlovesLootModifier.class, GlovesLootModifierAccessor.class);
        GlmAccessorUtils.registerGlobalLootModifier(registry, PigDropsModifier.class, PigDropsModifierAccessor.class);
        GlmAccessorUtils.registerGlobalLootModifier(registry, RemoveSeedsModifier.class, RemoveSeedsModifierAccessor.class);
    }
}
