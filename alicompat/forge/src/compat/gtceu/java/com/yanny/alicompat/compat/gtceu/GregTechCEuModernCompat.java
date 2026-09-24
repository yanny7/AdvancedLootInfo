package com.yanny.alicompat.compat.gtceu;

import com.gregtechceu.gtceu.data.loot.ChestGenHooks;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.alicompat.IModCompat;
import com.yanny.alicompat.accessor.PluginUtils;
import org.jetbrains.annotations.NotNull;

public class GregTechCEuModernCompat implements IModCompat {
    @NotNull
    @Override
    public String targetModId() {
        return GregTechCEuModernLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerEntry(registry, LootEntryItemAccessor.class);
        PluginUtils.registerEntryTooltip(registry, LootEntryItemAccessor.class);
        PluginUtils.registerFunctionTooltip(registry, ChestGenHooks.RandomWeightLootFunction.class, RandomWeightLootFunctionAccessor.class);
        PluginUtils.registerCountModifier(registry, ChestGenHooks.RandomWeightLootFunction.class, RandomWeightLootFunctionAccessor.class);
        PluginUtils.registerItemStackModifier(registry, ChestGenHooks.RandomWeightLootFunction.class, RandomWeightLootFunctionAccessor.class);
    }
}
