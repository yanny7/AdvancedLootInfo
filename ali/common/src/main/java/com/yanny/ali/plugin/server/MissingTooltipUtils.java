package com.yanny.ali.plugin.server;

import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.yanny.aci.language.CoreLang;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipContext;
import com.yanny.ali.api.IServerUtils;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.storage.loot.entries.CompositeEntryBase;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Objects;

import static com.yanny.ali.plugin.server.RegistriesTooltipUtils.*;

public class MissingTooltipUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    @NotNull
    public static TooltipBuilder getMissingEntryTooltip(IServerUtils utils, LootPoolEntryContainer entry) {
        TooltipBuilder tooltip = getEntryTypeTooltip(utils, entry.getType());

        try {
            RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, utils.lookupProvider());
            //noinspection unchecked
            MapCodec<LootPoolEntryContainer> codec = ((MapCodec<LootPoolEntryContainer>) entry.getType().codec());
            JsonElement jsonElement = codec.codec().encodeStart(registryOps, entry).getPartialOrThrow();

            tooltip.add(TooltipUtils.getJsonTooltip(utils, jsonElement));
        } catch (Throwable e) {
            if (utils.getConfiguration().logMoreStatistics) {
                LOGGER.warn("Failed to get entry info from serialized data for {} in {}", BuiltInRegistries.LOOT_POOL_ENTRY_TYPE.getKey(entry.getType()), TooltipContext.get(), e);
            }

            TooltipUtils.addObjectFields(utils, tooltip, entry, CompositeEntryBase.class);
        }

        return tooltip.key(CoreLang.Utils.AUTO_DETECTED);
    }

    @NotNull
    public static TooltipBuilder getMissingFunctionTooltip(IServerUtils utils, LootItemFunction function) {
        TooltipBuilder tooltip = getFunctionTypeTooltip(utils, function.getType());

        try {
            RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, utils.lookupProvider());
            //noinspection unchecked
            MapCodec<LootItemFunction> codec = ((MapCodec<LootItemFunction>) function.getType().codec());
            JsonElement jsonElement = codec.codec().encodeStart(registryOps, function).getPartialOrThrow();

            tooltip.add(TooltipUtils.getJsonTooltip(utils, jsonElement));
        } catch (Throwable e) {
            if (utils.getConfiguration().logMoreStatistics) {
                LOGGER.warn("Failed to get function info from serialized data for {} in {}", BuiltInRegistries.LOOT_FUNCTION_TYPE.getKey(function.getType()), TooltipContext.get(), e);
            }

            TooltipUtils.addObjectFields(utils, tooltip, function, LootItemFunction.class);
        }

        return tooltip.key(CoreLang.Utils.AUTO_DETECTED);
    }

    @NotNull
    public static TooltipBuilder getMissingConditionTooltip(IServerUtils utils, LootItemCondition condition) {
        TooltipBuilder tooltip = getConditionTypeTooltip(utils, condition.getType());

        try {
            RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, utils.lookupProvider());
            //noinspection unchecked
            MapCodec<LootItemCondition> codec = ((MapCodec<LootItemCondition>) condition.getType().codec());
            JsonElement jsonElement = codec.codec().encodeStart(registryOps, condition).getPartialOrThrow();

            tooltip.add(TooltipUtils.getJsonTooltip(utils, jsonElement));
        } catch (Throwable e) {
            if (utils.getConfiguration().logMoreStatistics) {
                LOGGER.warn("Failed to get condition info from serialized data for {} in {}", BuiltInRegistries.LOOT_CONDITION_TYPE.getKey(condition.getType()), TooltipContext.get(), e);
            }

            TooltipUtils.addObjectFields(utils, tooltip, condition, LootItemCondition.class);
        }

        return tooltip.key(CoreLang.Utils.AUTO_DETECTED);
    }

    @NotNull
    public static TooltipBuilder getMissingIngredientTooltip(IServerUtils utils, Ingredient ingredient) {
        TooltipBuilder tooltip = TooltipBuilder.value(ingredient.getClass().getName());

        try {
            RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, utils.lookupProvider());
            JsonElement jsonElement = Ingredient.CODEC.encodeStart(registryOps, ingredient).getPartialOrThrow();

            tooltip.add(TooltipUtils.getJsonTooltip(utils, jsonElement));
        } catch (Throwable e) {
            if (utils.getConfiguration().logMoreStatistics) {
                LOGGER.warn("Failed to get ingredient info from serialized data for {} in {}", ingredient.getClass().getName(), TooltipContext.get(), e);
            }

            TooltipUtils.addObjectFields(utils, tooltip, ingredient, Ingredient.class);
        }

        return tooltip.key(CoreLang.Utils.AUTO_DETECTED);
    }

    @NotNull
    public static TooltipBuilder getMissingItemSubPredicateTooltip(IServerUtils utils, ItemSubPredicate predicate) {
        TooltipBuilder tooltip = TooltipBuilder.value(predicate.getClass().getName());

        TooltipUtils.addObjectFields(utils, tooltip, predicate, ItemSubPredicate.class);
        return tooltip.key(CoreLang.Utils.AUTO_DETECTED);
    }

    @NotNull
    public static TooltipBuilder getMissingEntitySubPredicateTooltip(IServerUtils utils, EntitySubPredicate predicate) {
        TooltipBuilder tooltip = RegistriesTooltipUtils.getEntitySubPredicateTooltip(utils, predicate);

        try {
            RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, utils.lookupProvider());
            //noinspection unchecked
            MapCodec<EntitySubPredicate> codec = ((MapCodec<EntitySubPredicate>) predicate.codec());
            JsonElement jsonElement = codec.codec().encodeStart(registryOps, predicate).getPartialOrThrow();

            tooltip.add(TooltipUtils.getJsonTooltip(utils, jsonElement));
        } catch (Throwable e) {
            if (utils.getConfiguration().logMoreStatistics) {
                LOGGER.warn("Failed to get entity sub predicate info from serialized data for {} in {}", BuiltInRegistries.ENTITY_SUB_PREDICATE_TYPE.getKey(predicate.codec()), TooltipContext.get(), e);
            }

            TooltipUtils.addObjectFields(utils, tooltip, predicate, EntitySubPredicate.class);
        }

        return tooltip.key(CoreLang.Utils.AUTO_DETECTED);
    }

    @NotNull
    public static TooltipBuilder getMissingDataComponentTypeTooltip(IServerUtils utils, DataComponentType<?> type, Object value) {
        TooltipBuilder tooltip = RegistriesTooltipUtils.getDataComponentTypeTooltip(utils, type);

        try {
            RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, utils.lookupProvider());
            //noinspection unchecked
            Codec<Object> codec = ((Codec<Object>) type.codec());
            JsonElement jsonElement = Objects.requireNonNull(codec).encodeStart(registryOps, value).getPartialOrThrow();

            tooltip.add(TooltipUtils.getJsonTooltip(utils, jsonElement));
        } catch (Throwable e) {
            if (utils.getConfiguration().logMoreStatistics) {
                LOGGER.warn("Failed to get data component type info from serialized data for {} in {}", BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(type), TooltipContext.get(), e);
            }
        }

        return tooltip.key(CoreLang.Utils.AUTO_DETECTED);
    }


    @NotNull
    public static TooltipBuilder getMissingItemListingTooltip(IServerUtils utils, VillagerTrades.ItemListing itemListing) {
        TooltipBuilder tooltip = TooltipBuilder.value(itemListing.getClass().getName());

        TooltipUtils.addObjectFields(utils, tooltip, itemListing, VillagerTrades.ItemListing.class);
        return tooltip.key(CoreLang.Utils.AUTO_DETECTED);
    }

    @NotNull
    public static TooltipBuilder getMissingValueTooltip(IServerUtils ignoredUtils, Object object) {
        return TooltipBuilder.error("[" + object.getClass().getTypeName() + "]").key(CoreLang.Utils.NOT_IMPLEMENTED);
    }
}
