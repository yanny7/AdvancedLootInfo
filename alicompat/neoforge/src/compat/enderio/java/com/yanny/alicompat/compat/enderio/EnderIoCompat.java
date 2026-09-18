package com.yanny.alicompat.compat.enderio;

import com.enderio.enderio.api.conduits.ConduitIngredient;
import com.enderio.enderio.api.soul.binding.ingredients.AnySoulBindableIngredient;
import com.enderio.enderio.api.soul.binding.ingredients.EmptySoulBindableIngredient;
import com.enderio.enderio.api.soul.binding.ingredients.FilledSoulStorageIngredient;
import com.enderio.enderio.content.broken_spawner.BrokenSpawnerLootModifier;
import com.enderio.enderio.content.capacitors.SetLootCapacitorFunction;
import com.enderio.enderio.content.paint.CopyPaintFunction;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.alicompat.IGlmModCompat;
import com.yanny.alicompat.accessor.GlmAccessorUtils;
import com.yanny.alicompat.accessor.PluginUtils;
import org.jetbrains.annotations.NotNull;

public class EnderIoCompat implements IGlmModCompat {
    @NotNull
    @Override
    public String targetModId() {
        return EnderIoLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerFunctionTooltip(registry, SetLootCapacitorFunction.class, SetLootCapacitorFunctionAccessor.class);
        PluginUtils.registerFunctionTooltip(registry, CopyPaintFunction.class, CopyPaintFunctionAccessor.class);

        registry.registerValueTooltip(ConduitIngredient.class, EnderIoCompat::getConduitIngredientTooltip);
        PluginUtils.registerValueTooltip(registry, AnySoulBindableIngredient.class, AnySoulBindableIngredientAccessor.class);
        PluginUtils.registerValueTooltip(registry, EmptySoulBindableIngredient.class, EmptySoulBindableIngredientAccessor.class);
        PluginUtils.registerValueTooltip(registry, FilledSoulStorageIngredient.class, FilledSoulStorageIngredientAccessor.class);
    }

    @Override
    public void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry) {
        GlmAccessorUtils.registerGlobalLootModifier(registry, BrokenSpawnerLootModifier.class, BrokenSpawnerLootModifierAccessor.class);
    }

    @NotNull
    private static TooltipBuilder getConduitIngredientTooltip(IServerUtils utils, ConduitIngredient ingredient) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, ingredient.conduit().unwrapKey())), EnderIoLang.Ingredient.CONDUIT);
    }
}
