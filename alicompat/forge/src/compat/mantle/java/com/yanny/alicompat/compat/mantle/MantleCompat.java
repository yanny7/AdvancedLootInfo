package com.yanny.alicompat.compat.mantle;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.alicompat.IGlmModCompat;
import com.yanny.alicompat.accessor.GlmAccessorUtils;
import com.yanny.alicompat.accessor.PluginUtils;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import slimeknights.mantle.loot.AddEntryLootModifier;
import slimeknights.mantle.loot.ReplaceItemLootModifier;
import slimeknights.mantle.loot.condition.BlockTagLootCondition;
import slimeknights.mantle.loot.condition.HasLootContextSetCondition;
import slimeknights.mantle.loot.entry.TagPreferenceLootEntry;
import slimeknights.mantle.loot.function.RetexturedLootFunction;
import slimeknights.mantle.loot.function.SetFluidLootFunction;
import slimeknights.mantle.recipe.condition.TagEmptyCondition;
import slimeknights.mantle.recipe.condition.TagFilledCondition;
import slimeknights.mantle.recipe.data.ItemNameIngredient;
import slimeknights.mantle.recipe.data.NBTNameIngredient;
import slimeknights.mantle.recipe.ingredient.FluidContainerIngredient;
import slimeknights.mantle.recipe.ingredient.PotionDisplayIngredient;
import slimeknights.mantle.recipe.ingredient.PotionIngredient;

public class MantleCompat implements IGlmModCompat {
    @NotNull
    @Override
    public String targetModId() {
        return MantleLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerEntry(registry, TagPreferenceLootEntry.class, TagPreferenceLootEntryAccessor.class);
        PluginUtils.registerEntryTooltip(registry, TagPreferenceLootEntry.class, TagPreferenceLootEntryAccessor.class);

        PluginUtils.registerConditionTooltip(registry, BlockTagLootCondition.class, BlockTagLootConditionAccessor.class);
        PluginUtils.registerDestination(registry, BlockTagLootCondition.class, BlockTagLootConditionAccessor.class);
        PluginUtils.registerConditionTooltip(registry, HasLootContextSetCondition.class, HasLootContextSetConditionAccessor::new);
        registry.registerConditionTooltip(TagEmptyCondition.class, MantleCompat::getTagEmptyTooltip);
        registry.registerConditionTooltip(TagFilledCondition.class, MantleCompat::getTagFilledTooltip);

        PluginUtils.registerFunctionTooltip(registry, RetexturedLootFunction.class, RetexturedLootFunctionAccessor::new);
        PluginUtils.registerFunctionTooltip(registry, SetFluidLootFunction.class, SetFluidLootFunctionAccessor.class);

        PluginUtils.registerIngredientTooltip(registry, FluidContainerIngredient.class, FluidContainerIngredientAccessor.class);
        PluginUtils.registerIngredientTooltip(registry, ItemNameIngredient.class, ItemNameIngredientAccessor.class);
        PluginUtils.registerIngredientTooltip(registry, NBTNameIngredient.class, NBTNameIngredientAccessor.class);
        PluginUtils.registerIngredientTooltip(registry, PotionIngredient.class, PotionIngredientAccessor.class);
        PluginUtils.registerIngredientTooltip(registry, PotionDisplayIngredient.class, PotionDisplayIngredientAccessor.class);

        registry.registerValueTooltip(FluidStack.class, MantleCompat::getFluidStackTooltip);
        registry.registerValueTooltip(LootContextParamSet.class, MantleCompat::getLootContextParamSetTooltip);
    }

    @Override
    public void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry) {
        GlmAccessorUtils.registerGlobalLootModifier(registry, AddEntryLootModifier.class, AddEntryLootModifierAccessor.class);
        GlmAccessorUtils.registerGlobalLootModifier(registry, ReplaceItemLootModifier.class, ReplaceItemLootModifierAccessor.class);
    }

    @NotNull
    public static TooltipBuilder getTagEmptyTooltip(IServerUtils utils, TagEmptyCondition<?> cond) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, cond.getTag()).build(Lang.Value.TAG)), MantleLang.Conditions.TAG_EMPTY);
    }

    @NotNull
    public static TooltipBuilder getTagFilledTooltip(IServerUtils utils, TagFilledCondition<?> cond) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, cond.getTag()).build(Lang.Value.TAG)), MantleLang.Conditions.TAG_FILLED);
    }

    @NotNull
    private static TooltipBuilder getLootContextParamSetTooltip(IServerUtils utils, LootContextParamSet set) {
        return utils.getValueTooltip(utils, LootContextParamSets.getKey(set));
    }

    @NotNull
    private static TooltipBuilder getFluidStackTooltip(IServerUtils utils, FluidStack fluid) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fluid.getFluid()).build(Lang.Value.FLUID));
            b.add(utils.getValueTooltip(utils, fluid.getAmount()).build(Lang.Value.AMOUNT));
            b.add(utils.getValueTooltip(utils, fluid.getTag()).build(Lang.Value.NBT));
        });
    }
}
