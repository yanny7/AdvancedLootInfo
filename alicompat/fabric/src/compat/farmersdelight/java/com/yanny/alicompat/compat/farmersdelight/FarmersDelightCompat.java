package com.yanny.alicompat.compat.farmersdelight;

import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.ali.plugin.glm.ILootTableIdConditionPredicate;
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
        registry.registerFunctionTooltip(CopySkilletFunction.class, (utils, function) -> new CopySkilletFunctionAccessor(function).getTooltip(utils));
        registry.registerFunctionTooltip(CopyMealFunction.class, (utils, function) -> new CopyMealFunctionAccessor(function).getTooltip(utils));

        PluginUtils.registerItemListing(registry, FDItemListingAccessor.class);
    }

    @Override
    public void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry, ILootTableIdConditionPredicate predicate) {
        GlmAccessorUtils.registerGlobalLootModifier(registry, AddItemModifier.class, AddItemModifierAccessor.class, predicate);
        GlmAccessorUtils.registerGlobalLootModifier(registry, AddLootTableModifier.class, AddLootTableModifierAccessor.class, predicate);
        GlmAccessorUtils.registerGlobalLootModifier(registry, PastrySlicingModifier.class, PastrySlicingModifierAccessor.class, predicate);
        GlmAccessorUtils.registerGlobalLootModifier(registry, ReplaceItemModifier.class, ReplaceItemModifierAccessor.class, predicate);
    }
}
