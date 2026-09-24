package com.yanny.ali.forge.plugin;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.*;
import com.yanny.ali.forge.mixin.MixinBasicItemListing;
import com.yanny.ali.forge.mixin.MixinCanToolPerformAction;
import com.yanny.ali.forge.mixin.MixinForgeInternalHandler;
import com.yanny.ali.forge.mixin.MixinLootModifier;
import com.yanny.ali.forge.mixin.MixinLootModifierManager;
import com.yanny.ali.forge.mixin.MixinLootTableIdCondition;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.ali.plugin.glm.GlobalLootModifierCollector;
import com.yanny.ali.plugin.glm.GlobalLootModifierWrapper;
import com.yanny.ali.plugin.glm.IGlobalLootModifierWrapper;
import com.yanny.ali.plugin.glm.IPageLootModifier;
import com.yanny.ali.plugin.glm.LootPage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraftforge.common.BasicItemListing;
import net.minecraftforge.common.crafting.CompoundIngredient;
import net.minecraftforge.common.crafting.DifferenceIngredient;
import net.minecraftforge.common.crafting.IntersectionIngredient;
import net.minecraftforge.common.crafting.PartialNBTIngredient;
import net.minecraftforge.common.crafting.StrictNBTIngredient;
import net.minecraftforge.common.loot.*;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@AliEntrypoint
public class ForgePlugin implements IPlugin {
    @NotNull
    @Override
    public String getModId() {
        return "forge";
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerConditionTooltip(CanToolPerformAction.class, ForgePlugin::getCanToolPerformActionTooltip);
        registry.registerConditionTooltip(LootTableIdCondition.class, ForgePlugin::getLootTableIdTooltip);

        registry.registerIngredientTooltip(CompoundIngredient.class, ForgeIngredientTooltipUtils::getCompoundIngredientTooltip);
        registry.registerIngredientTooltip(DifferenceIngredient.class, ForgeIngredientTooltipUtils::getDifferenceIngredientTooltip);
        registry.registerIngredientTooltip(IntersectionIngredient.class, ForgeIngredientTooltipUtils::getIntersectionIngredientTooltip);
        registry.registerIngredientTooltip(PartialNBTIngredient.class, ForgeIngredientTooltipUtils::getPartialNbtIngredientTooltip);
        registry.registerIngredientTooltip(StrictNBTIngredient.class, ForgeIngredientTooltipUtils::getStrictNbtIngredientTooltip);

        registry.registerLootContextPreparer(ForgePlugin::prepareLootContext);

        registry.registerItemListing(BasicItemListing.class, ForgePlugin::getBasicItemListingNode);

        registry.registerGlobalLootModifiers(ForgePlugin::registerLootModifiers);
    }

    @NotNull
    public static ItemsToItemsNode getBasicItemListingNode(IServerUtils utils, BasicItemListing listing, TooltipNode condition) {
        MixinBasicItemListing accessor = (MixinBasicItemListing) listing;

        return new ItemsToItemsNode(
                utils,
                Either.left(accessor.getPrice()),
                new RangeValue(accessor.getPrice().getCount()),
                Either.left(accessor.getPrice2()),
                new RangeValue(accessor.getPrice2().getCount()),
                Either.left(accessor.getForSale()),
                new RangeValue(accessor.getForSale().getCount()),
                accessor.getMaxTrades(),
                accessor.getXp(),
                accessor.getPriceMult(),
                condition
        );
    }

    @NotNull
    public static TooltipBuilder getCanToolPerformActionTooltip(IServerUtils utils, CanToolPerformAction cond) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, ((MixinCanToolPerformAction) cond).getAction().name())), Lang.Conditions.CAN_TOOL_PERFORM_ACTION);
    }

    @NotNull
    public static TooltipBuilder getLootTableIdTooltip(IServerUtils utils, LootTableIdCondition cond) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, ((MixinLootTableIdCondition) cond).getTargetLootTableId())), Lang.Conditions.LOOT_TABLE_ID);
    }

    private static void prepareLootContext(IServerUtils ignoredUtils, LootContext context, LootPage page) {
        context.setQueriedLootTableId(page.tableId());
    }

    @NotNull
    private static List<IPageLootModifier> registerLootModifiers(IServerUtils utils) {
        MixinLootModifierManager manager = (MixinLootModifierManager) MixinForgeInternalHandler.getLootModifierManager();

        return GlobalLootModifierCollector.collect(utils, manager.getAliRegisteredLootModifiers().entrySet().stream().map(ForgePlugin::wrap).toList());
    }

    @NotNull
    private static IGlobalLootModifierWrapper wrap(Map.Entry<ResourceLocation, IGlobalLootModifier> entry) {
        IGlobalLootModifier modifier = entry.getValue();

        return new GlobalLootModifierWrapper(
                entry.getKey(),
                modifier,
                LootModifier.class,
                () -> Arrays.asList(((MixinLootModifier) modifier).getAliConditions()),
                () -> serialize(modifier)
        );
    }

    @NotNull
    private static JsonElement serialize(IGlobalLootModifier modifier) {
        //noinspection unchecked
        Codec<IGlobalLootModifier> codec = ((Codec<IGlobalLootModifier>) modifier.codec());
        return codec.encodeStart(JsonOps.INSTANCE, modifier).getOrThrow(false, (s) -> {});
    }
}
