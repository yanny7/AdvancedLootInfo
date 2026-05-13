package com.yanny.ali.plugin.server;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.yanny.aci.language.CoreLang;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipContext;
import com.yanny.ali.api.IServerUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.storage.loot.Deserializers;
import net.minecraft.world.level.storage.loot.entries.CompositeEntryBase;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import static com.yanny.ali.plugin.server.RegistriesTooltipUtils.*;

public class MissingTooltipUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    @NotNull
    public static TooltipBuilder getMissingEntryTooltip(IServerUtils utils, LootPoolEntryContainer entry) {
        TooltipBuilder tooltip = getEntryTypeTooltip(utils, entry.getType());

        try {
            Gson lootGson = Deserializers.createLootTableSerializer().create();
            JsonElement jsonElement = lootGson.toJsonTree(entry);

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
            Gson lootGson = Deserializers.createFunctionSerializer().create();
            JsonElement jsonElement = lootGson.toJsonTree(function);

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
            Gson lootGson = Deserializers.createConditionSerializer().create();
            JsonElement jsonElement = lootGson.toJsonTree(condition);

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
            tooltip.add(TooltipUtils.getJsonTooltip(utils, ingredient.toJson()));
        } catch (Throwable e) {
            if (utils.getConfiguration().logMoreStatistics) {
                LOGGER.warn("Failed to get ingredient info from serialized data for {} in {}", ingredient.getClass().getName(), TooltipContext.get(), e);
            }

            TooltipUtils.addObjectFields(utils, tooltip, ingredient, Ingredient.class);
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
