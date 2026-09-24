package com.yanny.alicompat.compat.kaleidoscopecookery;

import com.github.ysbbbbbb.kaleidoscopecookery.item.RecipeItem;
import com.github.ysbbbbbb.kaleidoscopecookery.loot.AdditionLootModifier;
import com.github.ysbbbbbb.kaleidoscopecookery.loot.AdvanceBlockMatchTool;
import com.github.ysbbbbbb.kaleidoscopecookery.loot.AdvanceEntityMatchTool;
import com.github.ysbbbbbb.kaleidoscopecookery.loot.RecipeRandomlyFunction;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.alicompat.IGlmModCompat;
import com.yanny.alicompat.accessor.GlmAccessorUtils;
import com.yanny.alicompat.accessor.PluginUtils;
import org.jetbrains.annotations.NotNull;

public class KaleidoscopeCookeryCompat implements IGlmModCompat {
    @NotNull
    @Override
    public String targetModId() {
        return KaleidoscopeCookeryLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerConditionTooltip(registry, AdvanceBlockMatchTool.class, AdvanceBlockMatchToolAccessor.class);
        PluginUtils.registerConditionTooltip(registry, AdvanceEntityMatchTool.class, AdvanceEntityMatchToolAccessor.class);
        PluginUtils.registerFunctionTooltip(registry, RecipeRandomlyFunction.class, RecipeRandomlyFunctionAccessor.class);
        PluginUtils.registerItemListing(registry, EnchantedItemForEmeraldsAccessor.class);
        PluginUtils.registerPageResolver(registry, AdditionLootModifier.class, AdditionLootModifierAccessor.class);
        registry.registerValueTooltip(RecipeItem.RecipeRecord.class, KaleidoscopeCookeryCompat::getRecipeRecordTooltip);
    }

    @Override
    public void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry) {
        GlmAccessorUtils.registerGlobalLootModifier(registry, AdditionLootModifier.class, AdditionLootModifierAccessor.class);
    }

    @NotNull
    public static TooltipBuilder getRecipeRecordTooltip(IServerUtils utils, RecipeItem.RecipeRecord record) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, record.input()).build(KaleidoscopeCookeryLang.Branch.INPUTS));
            b.add(utils.getValueTooltip(utils, record.output()).build(KaleidoscopeCookeryLang.Branch.OUTPUT));
            b.add(utils.getValueTooltip(utils, record.type()).build(KaleidoscopeCookeryLang.Value.RECIPE_TYPE));
            b.add(utils.getValueTooltip(utils, record.flexRecipe()).build(KaleidoscopeCookeryLang.Value.FLEX_RECIPE));
        });
    }
}
