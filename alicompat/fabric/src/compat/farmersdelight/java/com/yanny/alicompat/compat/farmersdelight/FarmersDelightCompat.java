package com.yanny.alicompat.compat.farmersdelight;

import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.alicompat.IGlmModCompat;
import com.yanny.alicompat.accessor.GlmAccessorUtils;
import com.yanny.alicompat.accessor.PluginUtils;
import org.jetbrains.annotations.NotNull;
import vectorwing.farmersdelight.common.loot.function.CopyMealFunction;
import vectorwing.farmersdelight.common.loot.function.CopySkilletFunction;
import vectorwing.farmersdelight.common.loot.modifier.AddItemModifier;
import vectorwing.farmersdelight.common.loot.modifier.AddLootTableModifier;
import vectorwing.farmersdelight.common.loot.modifier.PastrySlicingModifier;
import vectorwing.farmersdelight.common.loot.modifier.ReplaceItemModifier;

public class FarmersDelightCompat implements IGlmModCompat {
    @NotNull
    @Override
    public String targetModId() {
        return FarmersDelightLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerFunctionTooltip(registry, CopySkilletFunction.class, CopySkilletFunctionAccessor::new);
        PluginUtils.registerFunctionTooltip(registry, CopyMealFunction.class, CopyMealFunctionAccessor::new);

        PluginUtils.registerItemListing(registry, FDItemListingAccessor.class);
    }

    @Override
    public void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry) {
        GlmAccessorUtils.registerGlobalLootModifier(registry, AddItemModifier.class, AddItemModifierAccessor.class);
        GlmAccessorUtils.registerGlobalLootModifier(registry, AddLootTableModifier.class, AddLootTableModifierAccessor.class);
        GlmAccessorUtils.registerGlobalLootModifier(registry, PastrySlicingModifier.class, PastrySlicingModifierAccessor.class);
        GlmAccessorUtils.registerGlobalLootModifier(registry, ReplaceItemModifier.class, ReplaceItemModifierAccessor.class);
    }
}
