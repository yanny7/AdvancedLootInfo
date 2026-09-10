package com.yanny.alicompat.compat.immersiveengineering;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.common.crafting.fluidaware.IngredientFluidStack;
import blusunrize.immersiveengineering.common.register.IEItems;
import blusunrize.immersiveengineering.common.util.loot.BEDropLootEntry;
import blusunrize.immersiveengineering.common.util.loot.BluprintzLootFunction;
import blusunrize.immersiveengineering.common.util.loot.ConveyorCoverLootFunction;
import blusunrize.immersiveengineering.common.util.loot.DropInventoryLootEntry;
import blusunrize.immersiveengineering.common.util.loot.GrassDropModifier;
import blusunrize.immersiveengineering.common.util.loot.MultiblockDropsLootContainer;
import blusunrize.immersiveengineering.common.util.loot.PropertyCountLootFunction;
import blusunrize.immersiveengineering.common.util.loot.RevolverperkLootFunction;
import blusunrize.immersiveengineering.common.util.loot.WindmillLootFunction;
import com.yanny.aci.language.ITooltipKey;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.DynamicNode;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.ali.plugin.server.TooltipUtils;
import com.yanny.alicompat.IGlmModCompat;
import com.yanny.alicompat.accessor.GlmAccessorUtils;
import com.yanny.alicompat.accessor.PluginUtils;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ImmersiveEngineeringCompat implements IGlmModCompat {
    @NotNull
    @Override
    public String targetModId() {
        return ImmersiveEngineeringLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerEntry(BEDropLootEntry.class, ImmersiveEngineeringCompat::getDynamicNode);
        registry.registerEntry(DropInventoryLootEntry.class, ImmersiveEngineeringCompat::getDynamicNode);
        registry.registerEntry(MultiblockDropsLootContainer.class, ImmersiveEngineeringCompat::getDynamicNode);
        registry.registerEntryTooltip(BEDropLootEntry.class, ImmersiveEngineeringCompat::getTileDropTooltip);
        registry.registerEntryTooltip(DropInventoryLootEntry.class, ImmersiveEngineeringCompat::getDropInventoryTooltip);
        registry.registerEntryTooltip(MultiblockDropsLootContainer.class, ImmersiveEngineeringCompat::getMultiblockDropsTooltip);

        registry.registerFunctionTooltip(BluprintzLootFunction.class, ImmersiveEngineeringCompat::getSecretBluprintzTooltip);
        registry.registerFunctionTooltip(ConveyorCoverLootFunction.class, ImmersiveEngineeringCompat::getConveyorCoverTooltip);
        registry.registerFunctionTooltip(RevolverperkLootFunction.class, ImmersiveEngineeringCompat::getRevolverperkTooltip);
        registry.registerFunctionTooltip(WindmillLootFunction.class, ImmersiveEngineeringCompat::getWindmillTooltip);
        PluginUtils.registerFunctionTooltip(registry, PropertyCountLootFunction.class, PropertyCountLootFunctionAccessor.class);

        registry.registerIngredientTooltip(IngredientFluidStack.class, ImmersiveEngineeringCompat::getFluidStackIngredientTooltip);

        registry.registerValueTooltip(IEItems.ItemRegObject.class, ImmersiveEngineeringCompat::getItemRegObjectTooltip);
        PluginUtils.registerValueTooltip(registry, FluidTagInput.class, FluidTagInputAccessor.class);

        PluginUtils.registerItemListing(registry, TradeListingAccessor.class);
        PluginUtils.registerItemListing(registry, OreveinMapForEmeraldsAccessor.class);
        PluginUtils.registerItemListing(registry, RevolverPieceForEmeraldsAccessor.class);
    }

    @Override
    public void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry) {
        GlmAccessorUtils.registerGlobalLootModifier(registry, GrassDropModifier.class, GrassDropModifierAccessor.class);
    }

    @NotNull
    private static IDataNode getDynamicNode(IServerUtils utils, LootPoolSingletonContainer entry, float rawChance, int sumWeight,
                                            List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        List<LootItemFunction> allFunctions = NodeUtils.getAllFunctions(entry, functions);
        List<LootItemCondition> allConditions = NodeUtils.getAllConditions(entry, conditions);
        float chance = NodeUtils.getChance(entry, rawChance, sumWeight);

        return new DynamicNode(chance, TooltipUtils.getDynamicTooltip(utils, entry.quality, chance, allFunctions, allConditions).build());
    }

    @NotNull
    private static TooltipBuilder getTileDropTooltip(IServerUtils utils, BEDropLootEntry entry) {
        return getSingletonTooltip(utils, entry, ImmersiveEngineeringLang.Entry.TILE_DROP);
    }

    @NotNull
    private static TooltipBuilder getDropInventoryTooltip(IServerUtils utils, DropInventoryLootEntry entry) {
        return getSingletonTooltip(utils, entry, ImmersiveEngineeringLang.Entry.DROP_INVENTORY);
    }

    @NotNull
    private static TooltipBuilder getMultiblockDropsTooltip(IServerUtils utils, MultiblockDropsLootContainer entry) {
        return getSingletonTooltip(utils, entry, ImmersiveEngineeringLang.Entry.MULTIBLOCK_DROPS);
    }

    @NotNull
    private static TooltipBuilder getSingletonTooltip(IServerUtils utils, LootPoolSingletonContainer entry, ITooltipKey key) {
        return TooltipBuilder.array((b) -> {
            b.add(TooltipUtils.getWeightTooltip(entry.weight));
            b.add(TooltipUtils.getQualityTooltip(entry.quality));
            b.add(utils.getValueTooltip(utils, entry.conditions).build(Lang.Branch.PREDICATES));
            b.add(utils.getValueTooltip(utils, entry.functions).build(Lang.Branch.MODIFIERS));
        }, key);
    }

    @NotNull
    private static TooltipBuilder getSecretBluprintzTooltip(IServerUtils utils, BluprintzLootFunction fun) {
        return getConditionalFunctionTooltip(utils, fun.predicates, ImmersiveEngineeringLang.Functions.SECRET_BLUPRINTZ);
    }

    @NotNull
    private static TooltipBuilder getConveyorCoverTooltip(IServerUtils utils, ConveyorCoverLootFunction fun) {
        return getConditionalFunctionTooltip(utils, fun.predicates, ImmersiveEngineeringLang.Functions.CONVEYOR_COVER);
    }

    @NotNull
    private static TooltipBuilder getRevolverperkTooltip(IServerUtils utils, RevolverperkLootFunction fun) {
        return getConditionalFunctionTooltip(utils, fun.predicates, ImmersiveEngineeringLang.Functions.REVOLVERPERK);
    }

    @NotNull
    private static TooltipBuilder getWindmillTooltip(IServerUtils utils, WindmillLootFunction fun) {
        return getConditionalFunctionTooltip(utils, fun.predicates, ImmersiveEngineeringLang.Functions.WINDMILL);
    }

    @NotNull
    private static TooltipBuilder getConditionalFunctionTooltip(IServerUtils utils, LootItemCondition[] predicates, ITooltipKey key) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, predicates).build(Lang.Branch.PREDICATES));
            b.showEmpty();
        }, key);
    }

    @NotNull
    private static TooltipBuilder getFluidStackIngredientTooltip(IServerUtils utils, IngredientFluidStack ingredient) {
        return utils.getValueTooltip(utils, ingredient.getFluidTagInput());
    }

    @NotNull
    private static TooltipBuilder getItemRegObjectTooltip(IServerUtils utils, IEItems.ItemRegObject<?> value) {
        return utils.getValueTooltip(utils, value.asItem());
    }
}
