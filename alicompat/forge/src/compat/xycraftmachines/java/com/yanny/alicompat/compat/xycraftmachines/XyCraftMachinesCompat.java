package com.yanny.alicompat.compat.xycraftmachines;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.alicompat.IGlmModCompat;
import com.yanny.alicompat.accessor.GlmAccessorUtils;
import com.yanny.alicompat.accessor.PluginUtils;
import org.jetbrains.annotations.NotNull;
import tv.soaryn.xycraft.machines.content.recipe.BlockIngredient;
import tv.soaryn.xycraft.machines.data.AutoSmeltLootModifier;

public class XyCraftMachinesCompat implements IGlmModCompat {
    @NotNull
    @Override
    public String targetModId() {
        return XyCraftMachinesLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerDestination(registry, AutoSmeltLootModifier.class, AutoSmeltLootModifierAccessor.class);

        registry.registerIngredientTooltip(BlockIngredient.BlockIngredientList.class, XyCraftMachinesCompat::getBlockIngredientListTooltip);
        registry.registerIngredientTooltip(BlockIngredient.TaggedBlockIngredient.class, XyCraftMachinesCompat::getTaggedBlockIngredientTooltip);
    }

    @Override
    public void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry) {
        GlmAccessorUtils.registerGlobalLootModifier(registry, AutoSmeltLootModifier.class, AutoSmeltLootModifierAccessor.class);
    }

    @NotNull
    public static TooltipBuilder getBlockIngredientListTooltip(IServerUtils utils, BlockIngredient.BlockIngredientList ingredient) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, ingredient.predicates).build(Lang.Branch.PROPERTIES));
            b.add(utils.getValueTooltip(utils, ingredient.blocks).build(Lang.Branch.BLOCKS));
        }, XyCraftMachinesLang.Ingredient.BLOCK_STATE);
    }

    @NotNull
    public static TooltipBuilder getTaggedBlockIngredientTooltip(IServerUtils utils, BlockIngredient.TaggedBlockIngredient ingredient) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, ingredient.predicates).build(Lang.Branch.PROPERTIES));
            b.add(utils.getValueTooltip(utils, ingredient.tag.getKey()).build(Lang.Value.TAG));
        }, XyCraftMachinesLang.Ingredient.BLOCK_STATE);
    }
}
