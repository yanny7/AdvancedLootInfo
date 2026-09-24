package com.yanny.alicompat.compat.immersiveengineering;

import blusunrize.immersiveengineering.api.IEApiDataComponents;
import blusunrize.immersiveengineering.common.crafting.fluidaware.IngredientFluidStack;
import blusunrize.immersiveengineering.common.register.IEItemSubPredicates;
import blusunrize.immersiveengineering.common.register.IEItems;
import blusunrize.immersiveengineering.common.util.loot.*;
import blusunrize.immersiveengineering.common.world.Villages;
import com.yanny.aci.language.ITooltipKey;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.DynamicNode;
import com.yanny.ali.plugin.common.nodes.MissingNode;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.ali.plugin.glm.LootPage;
import com.yanny.ali.plugin.glm.Verdict;
import com.yanny.ali.plugin.server.MissingTooltipUtils;
import com.yanny.ali.plugin.server.TooltipUtils;
import com.yanny.alicompat.IGlmModCompat;
import com.yanny.alicompat.accessor.GlmAccessorUtils;
import com.yanny.alicompat.accessor.PluginUtils;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ImmersiveEngineeringCompat implements IGlmModCompat {
    private static final String OREVEIN_MAP = "orevein_map";
    private static final String REVOLVER_PIECE = "revolver_piece";

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

        registry.registerConditionTooltip(LootBlockStateFromLocationPredicate.class, ImmersiveEngineeringCompat::getBlockStateFromLocationTooltip);

        registry.registerPageResolver(LootBlockStateFromLocationPredicate.class, ImmersiveEngineeringCompat::testBlockStateFromLocation);

        registry.registerItemSubPredicateTooltip(IEItemSubPredicates.ItemBlueprintPredicate.class, ImmersiveEngineeringCompat::getItemBlueprintPredicateTooltip);

        registry.registerValueTooltip(IngredientFluidStack.class, ImmersiveEngineeringCompat::getFluidStackIngredientTooltip);
        registry.registerValueTooltip(IEItems.ItemRegObject.class, ImmersiveEngineeringCompat::getItemRegObjectTooltip);

        PluginUtils.registerItemListing(registry, TradeListingAccessor.class);
        PluginUtils.registerItemListing(registry, GroupedListingAccessor.class);
        registry.registerItemListing(Villages.RerollingItemListing.class, ImmersiveEngineeringCompat::getRerollingListingNode);

        registry.registerDataComponentTypeTooltip(IEApiDataComponents.BLUEPRINT_TYPE.get(), ImmersiveEngineeringCompat::getBlueprintTooltip);
    }

    @NotNull
    private static IDataNode getRerollingListingNode(IServerUtils utils, Villages.RerollingItemListing listing, TooltipNode condition) {
        return switch (listing.functionKey()) {
            case OREVEIN_MAP -> new OreveinMapForEmeraldsAccessor(listing).getNode(utils, condition);
            case REVOLVER_PIECE -> new RevolverPieceForEmeraldsAccessor(listing).getNode(utils, condition);
            default -> new MissingNode(MissingTooltipUtils.getMissingItemListingTooltip(utils, listing).build());
        };
    }

    @NotNull
    private static TooltipBuilder getBlueprintTooltip(IServerUtils utils, String value) {
        return utils.getValueTooltip(utils, value).key(Lang.Value.VALUE);
    }

    @Override
    public void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry) {
        GlmAccessorUtils.registerGlobalLootModifier(registry, AddDropModifier.class, AddDropModifierAccessor.class);
    }

    @NotNull
    private static IDataNode getDynamicNode(IServerUtils utils, LootPoolSingletonContainer entry, float rawChance, int sumWeight,
                                            List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        List<LootItemFunction> allFunctions = NodeUtils.getAllFunctions(utils, entry, functions);
        List<LootItemCondition> allConditions = NodeUtils.getAllConditions(utils, entry, conditions);
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
            b.showEmpty();
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
    private static TooltipBuilder getConditionalFunctionTooltip(IServerUtils utils, List<LootItemCondition> predicates, ITooltipKey key) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, predicates).build(Lang.Branch.PREDICATES));
            b.showEmpty();
        }, key);
    }

    @NotNull
    private static TooltipBuilder getBlockStateFromLocationTooltip(IServerUtils utils, LootBlockStateFromLocationPredicate cond) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, cond.block()).build(Lang.Value.BLOCK));
            b.add(utils.getValueTooltip(utils, cond.properties()).build(Lang.Branch.PROPERTIES));
        }, ImmersiveEngineeringLang.Conditions.BLOCK_STATE_FROM_LOCATION);
    }

    @Nullable
    private static Verdict testBlockStateFromLocation(IServerUtils ignoredUtils, LootBlockStateFromLocationPredicate cond, LootPage page) {
        if (page.blocks().isEmpty()) {
            return null;
        }

        return GlobalLootModifierUtils.testBlocks(page, (b) -> cond.block().value().equals(b), cond.properties().isEmpty());
    }

    @NotNull
    private static TooltipBuilder getItemBlueprintPredicateTooltip(IServerUtils utils, IEItemSubPredicates.ItemBlueprintPredicate predicate) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, predicate.blueprint()).build(Lang.Value.VALUE)), ImmersiveEngineeringLang.ItemSubPredicates.BLUEPRINT);
    }

    @NotNull
    private static TooltipBuilder getFluidStackIngredientTooltip(IServerUtils utils, IngredientFluidStack ingredient) {
        List<FluidStack> fluids = List.of(ingredient.fluidIngredient().getFluids());

        return TooltipBuilder.array((b) -> {
            b.add(TooltipBuilder.array((c) -> fluids.forEach((f) -> c.add(TooltipBuilder.asElement(utils.getValueTooltip(utils, f.getFluid()), fluids.size())))).build(Lang.Branch.FLUIDS));
            b.add(utils.getValueTooltip(utils, ingredient.fluidIngredient().amount()).build(Lang.Value.AMOUNT));
        });
    }

    @NotNull
    private static TooltipBuilder getItemRegObjectTooltip(IServerUtils utils, IEItems.ItemRegObject<?> value) {
        return utils.getValueTooltip(utils, value.asItem());
    }
}
