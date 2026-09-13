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
import com.yanny.ali.forge.mixin.MixinLootTableIdCondition;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.ali.plugin.glm.Destination;
import com.yanny.ali.plugin.glm.GlobalLootModifierCollector;
import com.yanny.ali.plugin.glm.GlobalLootModifierWrapper;
import com.yanny.ali.plugin.glm.IGlobalLootModifierWrapper;
import net.minecraftforge.common.BasicItemListing;
import net.minecraftforge.common.crafting.CompoundIngredient;
import net.minecraftforge.common.crafting.DifferenceIngredient;
import net.minecraftforge.common.crafting.IntersectionIngredient;
import net.minecraftforge.common.crafting.PartialNBTIngredient;
import net.minecraftforge.common.crafting.StrictNBTIngredient;
import net.minecraftforge.common.loot.*;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

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

        registry.registerDestination(LootTableIdCondition.class, ForgePlugin::getLootTableIdDestination);

        registry.registerItemListing(BasicItemListing.class, ForgePlugin::getBasicItemListingNode);

        registry.registerLootModifiers(ForgePlugin::registerLootModifiers);
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

    @NotNull
    public static Destination getLootTableIdDestination(IServerUtils ignoredUtils, LootTableIdCondition cond) {
        return new Destination.Table(((MixinLootTableIdCondition) cond).getTargetLootTableId()::equals, true);
    }

    @NotNull
    private static List<ILootModifier<?>> registerLootModifiers(IServerUtils utils) {
        return GlobalLootModifierCollector.collect(utils, MixinForgeInternalHandler.getLootModifierManager().getAllLootMods().stream().map(ForgePlugin::wrap).toList());
    }

    @NotNull
    private static IGlobalLootModifierWrapper wrap(IGlobalLootModifier modifier) {
        return new GlobalLootModifierWrapper(
                ForgeRegistries.GLOBAL_LOOT_MODIFIER_SERIALIZERS.get().getKey(modifier.codec()),
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
