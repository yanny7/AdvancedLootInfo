package com.yanny.emi_loot_addon.compatibility;

import com.google.gson.JsonObject;
import com.yanny.emi_loot_addon.mixin.*;
import com.yanny.emi_loot_addon.network.LootCondition;
import com.yanny.emi_loot_addon.network.LootFunction;
import com.yanny.emi_loot_addon.network.LootGroup;
import com.yanny.emi_loot_addon.network.RangeValue;
import com.yanny.emi_loot_addon.network.condition.*;
import com.yanny.emi_loot_addon.network.function.*;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.yanny.emi_loot_addon.compatibility.EmiUtils.*;

public abstract class EmiBaseLoot extends BasicEmiRecipe {
    protected final LootGroup message;
    protected final List<ItemData> itemDataList;

    public EmiBaseLoot(EmiRecipeCategory category, ResourceLocation id, LootGroup message) {
        super(category, id, 9 * 18, 1024);
        this.message = message;
        itemDataList = ItemData.parse(message);
        outputs = itemDataList.stream()
                .map((d) -> d.item)
                .filter(Objects::nonNull)
                .map(EmiStack::of)
                .toList();
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        addWidgets(widgets, new int[]{0, 0});
    }

    @Override
    public Recipe<?> getBackingRecipe() {
        return null;
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    public void addWidgets(WidgetHolder widgetHolder, int[] pos) {
        for (ItemData itemData : itemDataList) {
            SlotWidget widget = new LootSlotWidget(EmiStack.of(itemData.item), pos[0], pos[1])
                    .setCount(itemData.count);

            widget.appendTooltip(getChance(itemData));
            getBonusChance(itemData).forEach(widget::appendTooltip);

            widget.appendTooltip(getCount(itemData));
            getBonusCount(itemData).forEach(widget::appendTooltip);

            getConditions(itemData.conditions, 0).forEach(widget::appendTooltip);
            getFunctions(itemData.functions, 0).forEach(widget::appendTooltip);

            widget.recipeContext(this);
            widgetHolder.add(widget);

            if ((pos[0] + 18) / (9*18) > 0) {
                pos[1] += 18;
            }

            pos[0] = (pos[0] + 18) % (9*18);
        }
    }

    @NotNull
    private static List<Component> getConditions(List<LootCondition> conditions, int pad) {
        List<Component> components = new LinkedList<>();

        for (LootCondition condition : conditions) {
            switch (condition.type) {
                case ALL_OF -> components.addAll(getAllOfCondition((AllOfCondition) condition, pad));
                case ANY_OF -> components.addAll(getAnyOfCondition((AnyOfCondition) condition, pad));
                case INVERTED -> components.addAll(getInvertedCondition((InvertedCondition) condition, pad));
                case SURVIVES_EXPLOSION,
                     KILLED_BY_PLAYER -> components.add(pad(pad, translatableType("emi.type.emi_loot_addon.condition", condition.type)));
                case ENTITY_PROPERTIES -> components.addAll(getEntityPropertiesCondition((EntityPropertiesCondition) condition, pad));
                case ENTITY_SCORES -> components.addAll(getEntityScoresCondition((EntityScoresCondition) condition, pad));
                case BLOCK_STATE_PROPERTY -> components.addAll(getBlockStatePropertyCondition((BlockStatePropertyCondition) condition, pad));
                case MATCH_TOOL -> components.addAll(getMatchToolCondition((MatchToolCondition) condition, pad));
                case DAMAGE_SOURCE_PROPERTIES -> components.addAll(getDamageSourceCondition((DamageSourcePropertiesCondition) condition, pad));
                case LOCATION_CHECK -> components.addAll(getLocationCheckCondition((LocationCheckCondition) condition, pad));
                case WEATHER_CHECK -> components.addAll(getWeatherCheckCondition((WeatherCheckCondition) condition, pad));
                case REFERENCE -> components.addAll(getReferenceCondition((ReferenceCondition) condition, pad));
                case TIME_CHECK -> components.addAll(getTimeCheckCondition((TimeCheckCondition) condition, pad));
                case VALUE_CHECK -> components.addAll(getValueCheckCondition((ValueCheckCondition) condition, pad));
                case LOOT_CONDITION_TYPE -> components.add(pad(pad, translatableType("emi.type.emi_loot_addon.condition", condition.type, value(((CanToolPerformActionCondition) condition).action))));
                case RANDOM_CHANCE,
                     TABLE_BONUS,
                     RANDOM_CHANCE_WITH_LOOTING -> {}
                default -> {
                    //TODO logger
                }
            }
        }

        return components;
    }

    @NotNull
    private static List<Component> getFunctions(List<LootFunction> functions, int pad) {
        List<Component> components = new LinkedList<>();

        for (LootFunction function : functions) {
            switch (function.type) {
                case ENCHANT_WITH_LEVELS -> components.addAll(getEnchantWithLevelsFunction((EnchantWithLevelsFunction) function, pad));
                case ENCHANT_RANDOMLY -> components.addAll(getEnchantRandomly((EnchantRandomlyFunction) function, pad));
                case SET_ENCHANTMENTS -> components.addAll(getSetEnchantments((SetEnchantmentsFunction) function, pad));
                case SET_NBT -> components.addAll(getSetNbtFunction((SetNbtFunction) function, pad));
                case SET_DAMAGE -> components.addAll(getSetDamageFunction((SetDamageFunction) function, pad));
                case SET_ATTRIBUTES -> components.addAll(getSetAttributesFunction((SetAttributesFunction) function, pad));
                case SET_NAME -> components.addAll(getSetNameFunction((SetNameFunction) function, pad));
                case EXPLORATION_MAP -> components.addAll(getExplorationMapFunction((ExplorationMapFunction) function, pad));
                case SET_STEW_EFFECT -> components.addAll(getSetStewEffectFunction((SetStewEffectFunction) function, pad));
                case COPY_NAME -> components.addAll(getCopyNameFunction((CopyNameFunction) function, pad));
                case SET_CONTENTS -> components.addAll(getSetContentsFunction((SetContentsFunction) function, pad));
                case SET_LOOT_TABLE -> components.addAll(getSetLootTableFunction((SetLootTableFunction) function, pad));
                case EXPLOSION_DECAY,
                     COPY_NBT -> components.add(pad(pad, translatableType("emi.type.emi_loot_addon.function", function.type)));
                case SET_LORE -> components.addAll(getSetLoreFunction((SetLoreFunction) function, pad));
                case FILL_PLAYER_HEAD -> components.addAll(getFillPlayerHeadFunction((FillPlayerHeadFunction) function, pad));
                case COPY_STATE -> components.addAll(getCopyStateFunction((CopyStateFunction) function, pad));
                case SET_BANNER_PATTERN -> components.addAll(getSetBannerPatternFunction((SetBannerPatternFunction) function, pad));
                case SET_POTION -> components.addAll(getSetPotionFunction((SetPotionFunction) function, pad));
                case SET_INSTRUMENT -> components.addAll(getSetInstrumentFunction((SetInstrumentFunction) function, pad));
                case REFERENCE -> components.addAll(getReferenceFunction((ReferenceFunction) function, pad));
                case APPLY_BONUS,
                     LOOTING_ENCHANT,
                     SET_COUNT,
                     LIMIT_COUNT,
                     FURNACE_SMELT -> {}
                    //TODO debug print these values
                default -> {
                    //TODO logger
                }
            }
        }

        return components;
    }

    @NotNull
    private static List<Component> getAllOfCondition(AllOfCondition condition, int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatableType("emi.type.emi_loot_addon.condition", condition.type)));
        components.addAll(getConditions(condition.terms, pad + 1));

        return components;
    }

    @NotNull
    private static List<Component> getAnyOfCondition(AnyOfCondition condition, int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatableType("emi.type.emi_loot_addon.condition", condition.type)));
        components.addAll(getConditions(condition.terms, pad + 1));

        return components;
    }

    @NotNull
    private static List<Component> getInvertedCondition(InvertedCondition condition, int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatableType("emi.type.emi_loot_addon.condition", condition.type)));
        components.addAll(getConditions(List.of(condition.term), pad + 1));

        return components;
    }

    @NotNull
    private static List<Component> getEntityPropertiesCondition(EntityPropertiesCondition condition, int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatableType("emi.type.emi_loot_addon.condition", condition.type)));
        addEntityPredicate(components, pad + 1, translatable("emi.property.condition.predicate.target", value(translatableType("emi.enum.target", condition.target))), condition.predicate);

        return components;
    }

    @NotNull
    private static List<Component> getEntityScoresCondition(EntityScoresCondition condition, int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatableType("emi.type.emi_loot_addon.condition", condition.type)));
        components.add(pad(pad + 1, translatable("emi.property.condition.predicate.target", value(translatableType("emi.enum.target", condition.target)))));
        components.add(pad(pad + 1, translatable("emi.property.condition.scores.score")));
        condition.scores.forEach((score, tuple) -> components.add(pad(pad + 2, keyValue(score, RangeValue.rangeToString(tuple.getA(), tuple.getB())))));

        return components;
    }

    @NotNull
    private static List<Component> getBlockStatePropertyCondition(BlockStatePropertyCondition condition, int pad) {
        List<Component> components = new LinkedList<>();

        addStateProperties(components, pad, translatableType("emi.type.emi_loot_addon.condition", condition.type), condition.properties);

        return components;
    }

    @NotNull
    private static List<Component> getMatchToolCondition(MatchToolCondition condition, int pad) {
        List<Component> components = new LinkedList<>();

        addItemPredicate(components, pad, translatableType("emi.type.emi_loot_addon.condition", condition.type), condition.predicate);

        return components;
    }

    @NotNull
    private static List<Component> getDamageSourceCondition(DamageSourcePropertiesCondition condition, int pad) {
        List<Component> components = new LinkedList<>();

        addDamageSourcePredicate(components, pad, translatableType("emi.type.emi_loot_addon.condition", condition.type), condition.predicate);

        return components;
    }

    @NotNull
    private static List<Component> getLocationCheckCondition(LocationCheckCondition condition, int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatableType("emi.type.emi_loot_addon.condition", condition.type)));
        addLocationPredicate(components, pad + 1, translatable("emi.property.condition.location_check.location"), condition.predicate);
        components.add(pad(pad + 1, translatable("emi.property.condition.location_check.offset")));
        components.add(pad(pad + 2, translatable("emi.property.condition.location_check.x", condition.offset.getX())));
        components.add(pad(pad + 2, translatable("emi.property.condition.location_check.y", condition.offset.getY())));
        components.add(pad(pad + 2, translatable("emi.property.condition.location_check.z", condition.offset.getZ())));

        return components;
    }

    @NotNull
    private static List<Component> getWeatherCheckCondition(WeatherCheckCondition condition, int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatableType("emi.type.emi_loot_addon.condition", condition.type)));

        if (condition.isRaining != null) {
            components.add(pad(pad + 1, translatable("emi.property.condition.weather_check.is_raining", condition.isRaining)));
        }

        if (condition.isThundering != null) {
            components.add(pad(pad + 1, translatable("emi.property.condition.weather_check.is_thundering", condition.isThundering)));
        }

        return components;
    }

    @NotNull
    private static List<Component> getReferenceCondition(ReferenceCondition condition, int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatableType("emi.type.emi_loot_addon.condition", condition.type, condition.name)));

        return components;
    }

    @NotNull
    private static List<Component> getTimeCheckCondition(TimeCheckCondition condition, int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatableType("emi.type.emi_loot_addon.condition", condition.type)));
        components.add(pad(pad + 1, translatable("emi.property.condition.time_check.period", condition.period)));
        components.add(pad(pad + 1, translatable("emi.property.condition.time_check.value", RangeValue.rangeToString(condition.min, condition.max))));

        return components;
    }

    @NotNull
    private static List<Component> getValueCheckCondition(ValueCheckCondition condition, int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatableType("emi.type.emi_loot_addon.condition", condition.type)));
        components.add(pad(pad + 1, translatable("emi.property.condition.value_check.provider", condition.provider)));
        components.add(pad(pad + 1, translatable("emi.property.condition.value_check.range", RangeValue.rangeToString(condition.min, condition.max))));

        return components;
    }

    @NotNull
    private static Component getCount(ItemData data) {
        return translatable("emi.description.emi_loot_addon.count", data.count);
    }

    @NotNull
    private static Component getChance(ItemData data) {
        if ((!data.rolls.isRange() && data.rolls.min() > 1) || (data.rolls.isRange() && data.rolls.max() > 1)) {
            return translatable("emi.description.emi_loot_addon.chance_rolls", value(data.rolls.toIntString(), "x"), value(data.chance, "%"));
        }

        return translatable("emi.description.emi_loot_addon.chance", value(data.chance, "%"));
    }

    @NotNull
    private static List<Component> getBonusChance(ItemData data) {
        List<Component> components = new LinkedList<>();

        if (data.bonusChance != null) {
            data.bonusChance.getValue().forEach((level, value) -> components.add(pad(1, translatable(
                    "emi.description.emi_loot_addon.chance_bonus",
                    value(value, "%"),
                    Component.translatable(data.bonusChance.getKey().getDescriptionId()),
                    Component.translatable("enchantment.level." + level)
            ))));
        }

        return components;
    }

    @NotNull
    private static List<Component> getBonusCount(ItemData data) {
        List<Component> components = new LinkedList<>();

        if (data.bonusCount != null) {
            data.bonusCount.getValue().forEach((level, value) -> components.add(pad(1, translatable(
                    "emi.description.emi_loot_addon.count_bonus",
                    value,
                    Component.translatable(data.bonusCount.getKey().getDescriptionId()),
                    Component.translatable("enchantment.level." + level)
            ))));
        }

        return components;
    }

    @NotNull
    private static List<Component> getEnchantWithLevelsFunction(EnchantWithLevelsFunction function, int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatableType("emi.type.emi_loot_addon.function", function.type)));
        components.add(pad(pad + 1, translatable("emi.property.function.enchant_with_levels.levels", function.levels)));
        components.add(pad(pad + 1, translatable("emi.property.function.enchant_with_levels.treasure", function.treasure)));

        return components;
    }

    @NotNull
    private static List<Component> getEnchantRandomly(EnchantRandomlyFunction function, int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatableType("emi.type.emi_loot_addon.function", function.type)));

        if (!function.enchantments.isEmpty()) {
            components.add(pad(pad + 1, translatable("emi.property.function.enchant_randomly.enchantments")));

            function.enchantments.forEach((enchantment) -> {
                components.add(pad(pad + 2, translatable(ForgeRegistries.ENCHANTMENTS.getValue(enchantment).getDescriptionId())));
            });
        }

        return components;
    }

    @NotNull
    private static List<Component> getSetEnchantments(SetEnchantmentsFunction function, int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatableType("emi.type.emi_loot_addon.function", function.type)));
        components.add(pad(pad + 1, translatable("emi.property.function.set_enchantments.enchantments")));
        function.enchantments.forEach((enchantment, level) -> components.add(pad(pad + 2, translatable(
                "emi.property.function.set_enchantments.enchantment",
                translatable(ForgeRegistries.ENCHANTMENTS.getValue(enchantment).getDescriptionId()),
                level
        ))));
        components.add(pad(pad + 1, translatable("emi.property.function.set_enchantments.add", function.add)));

        return components;
    }

    @NotNull
    private static List<Component> getSetNbtFunction(SetNbtFunction function, int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatableType("emi.type.emi_loot_addon.function", function.type)));
        components.add(pad(pad + 1, value(function.tag)));

        return components;
    }

    @NotNull
    private static List<Component> getSetDamageFunction(SetDamageFunction function, int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatableType("emi.type.emi_loot_addon.function", function.type)));
        components.add(pad(pad + 1, translatable("emi.property.function.set_damage.damage", function.damage)));
        components.add(pad(pad + 1, translatable("emi.property.function.set_damage.add", function.add)));

        return components;
    }

    @NotNull
    private static List<Component> getSetAttributesFunction(SetAttributesFunction function, int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatableType("emi.type.emi_loot_addon.function", function.type)));

        function.modifiers.forEach((modifier) -> {
            components.add(pad(pad + 1, translatable("emi.property.function.set_attributes.name", modifier.name())));
            components.add(pad(pad + 2, translatable("emi.property.function.set_attributes.attribute", value(translatable(ForgeRegistries.ATTRIBUTES.getValue(modifier.attribute()).getDescriptionId())))));
            components.add(pad(pad + 2, translatable("emi.property.function.set_attributes.operation", AttributeModifier.Operation.fromValue(modifier.operation()))));
            components.add(pad(pad + 2, translatable("emi.property.function.set_attributes.amount", modifier.amount())));

            if (modifier.id() != null) {
                components.add(pad(pad + 2, translatable("emi.property.function.set_attributes.id", modifier.id())));
            }

            if (!modifier.slots().isEmpty()) {
                components.add(pad(pad + 2, translatable("emi.property.function.set_attributes.slots")));
                modifier.slots().forEach((slot) -> components.add(pad(pad + 3, value(slot))));
            }
        });

        return components;
    }

    @NotNull
    private static List<Component> getSetNameFunction(SetNameFunction function, int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatableType("emi.type.emi_loot_addon.function", function.type)));
        components.add(pad(pad + 1, translatable("emi.property.function.set_name.name", function.name)));

        if (function.resolutionContext != null) {
            components.add(pad(pad + 1, translatable("emi.property.function.set_name.resolution_context", translatableType("emi.enum.target", function.resolutionContext))));
        }

        return components;
    }

    @NotNull
    private static List<Component> getExplorationMapFunction(ExplorationMapFunction function, int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatableType("emi.type.emi_loot_addon.function", function.type)));
        components.add(pad(pad + 1, translatable("emi.property.function.exploration_map.destination", function.structure)));
        components.add(pad(pad + 1, translatable("emi.property.function.exploration_map.map_decoration", MapDecoration.Type.byIcon(function.mapDecoration))));
        components.add(pad(pad + 1, translatable("emi.property.function.exploration_map.zoom", function.zoom)));
        components.add(pad(pad + 1, translatable("emi.property.function.exploration_map.search_radius", function.searchRadius)));
        components.add(pad(pad + 1, translatable("emi.property.function.exploration_map.skip_known_structures", function.skipKnownStructures)));

        return components;
    }

    @NotNull
    private static List<Component> getSetStewEffectFunction(SetStewEffectFunction function, int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatableType("emi.type.emi_loot_addon.function", function.type)));

        function.effectMap.forEach((effect, duration) -> {
            components.add(pad(pad + 1, translatable("emi.property.function.set_stew_effect.effect", translatable(ForgeRegistries.MOB_EFFECTS.getValue(effect).getDescriptionId()), duration)));
        });

        return components;
    }

    @NotNull
    private static List<Component> getCopyNameFunction(CopyNameFunction function, int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatableType("emi.type.emi_loot_addon.function", function.type)));
        components.add(pad(pad + 1, translatable("emi.property.function.copy_name.source", value(translatableType("emi.enum.name_source",
                net.minecraft.world.level.storage.loot.functions.CopyNameFunction.NameSource.getByName(function.source))))));

        return components;
    }

    @NotNull
    private static List<Component> getSetContentsFunction(SetContentsFunction function, int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatableType("emi.type.emi_loot_addon.function", function.type)));
        components.add(pad(pad + 1, translatable("emi.property.function.set_contents.type", function.blockEntityType)));

        return components;
    }

    @NotNull
    private static List<Component> getSetLootTableFunction(SetLootTableFunction function, int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatableType("emi.type.emi_loot_addon.function", function.type)));
        components.add(pad(pad + 1, translatable("emi.property.function.set_loot_table.name", function.name)));
        components.add(pad(pad + 1, translatable("emi.property.function.set_loot_table.seed", function.seed)));
        components.add(pad(pad + 1, translatable("emi.property.function.set_loot_table.type", function.blockEntityType)));

        return components;
    }

    @NotNull
    private static List<Component> getSetLoreFunction(SetLoreFunction function, int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatableType("emi.type.emi_loot_addon.function", function.type)));
        components.add(pad(pad + 1, translatable("emi.property.function.set_lore.replace", function.replace)));

        if (function.resolutionContext != null) {
            components.add(pad(pad + 1, translatable("emi.property.function.set_lore.resolution_context", value(translatableType("emi.enum.target", function.resolutionContext)))));
        }

        components.add(pad(pad + 1, translatable("emi.property.function.set_lore.lore")));
        function.lore.forEach((l) -> components.add(pad(pad + 2, l)));

        return components;
    }

    @NotNull
    private static List<Component> getFillPlayerHeadFunction(FillPlayerHeadFunction function, int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatableType("emi.type.emi_loot_addon.function", function.type)));
        components.add(pad(pad + 1, translatable("emi.property.function.fill_player_head.target", function.entityTarget)));

        return components;
    }

    @NotNull
    private static List<Component> getCopyStateFunction(CopyStateFunction function, int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatableType("emi.type.emi_loot_addon.function", function.type)));
        components.add(pad(pad + 1, translatable("emi.property.function.copy_state.block", value(translatable(function.block.getDescriptionId())))));

        if (!function.properties.isEmpty()) {
            components.add(pad(pad + 1, translatable("emi.property.function.copy_state.properties", function.properties)));
            function.properties.forEach((property) -> components.add(pad(pad + 2, value(property.getName()))));
        }

        return components;
    }

    @NotNull
    private static List<Component> getSetBannerPatternFunction(SetBannerPatternFunction function, int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatableType("emi.type.emi_loot_addon.function", function.type)));
        components.add(pad(pad + 1, translatable("emi.property.function.set_banner_pattern.append", function.append)));

        if (!function.patterns.isEmpty()) {
            components.add(pad(pad + 1, translatable("emi.property.function.set_banner_pattern.patterns")));

            function.patterns.forEach((pair) -> {
                components.add(pad(pad + 2, value(BuiltInRegistries.BANNER_PATTERN.getKey(pair.getFirst().get()))));
                components.add(pad(pad + 3, translatable("emi.property.function.set_banner_pattern.color", pair.getSecond().getName())));
            });
        }

        return components;
    }

    @NotNull
    private static List<Component> getSetPotionFunction(SetPotionFunction function, int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatableType("emi.type.emi_loot_addon.function", function.type)));
        components.add(pad(pad + 1, translatable("emi.property.function.set_potion.name", function.potion.getName(""))));

        if (!function.potion.getEffects().isEmpty()) {
            components.add(pad(pad + 1, translatable("emi.property.function.set_potion.effects")));

            function.potion.getEffects().forEach((effect) -> components.add(pad(pad + 2, value(translatable(effect.getDescriptionId())))));
        }

        return components;
    }

    @NotNull
    private static List<Component> getSetInstrumentFunction(SetInstrumentFunction function, int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatableType("emi.type.emi_loot_addon.function", function.type)));
        components.add(pad(pad + 1, translatable("emi.property.function.set_instrument.options", function.options.location())));


        return components;
    }

    @NotNull
    private static List<Component> getReferenceFunction(ReferenceFunction function, int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatableType("emi.type.emi_loot_addon.function", function.type)));
        components.add(pad(pad + 1, translatable("emi.property.function.reference.name", function.name)));

        return components;
    }

    private static void addEntityPredicate(List<Component> components, int pad, Component component, EntityPredicate entityPredicate) {
        if (entityPredicate != EntityPredicate.ANY) {
            MixinEntityPredicate predicate = (MixinEntityPredicate) entityPredicate;

            components.add(pad(pad, component));

            addEntityTypePredicate(components, pad + 1, translatable("emi.property.condition.predicate.entity_type"), predicate.getEntityType());
            addDistancePredicate(components, pad + 1, translatable("emi.property.condition.predicate.dist_to_player"), predicate.getDistanceToPlayer());
            addLocationPredicate(components, pad + 1, translatable("emi.property.condition.predicate.location"), predicate.getLocation());
            addLocationPredicate(components, pad + 1, translatable("emi.property.condition.predicate.stepping_on_location"), predicate.getSteppingOnLocation());
            addMobEffectsPredicate(components, pad + 1, translatable("emi.property.condition.predicate.effect"), predicate.getEffects());
            addNbtPredicate(components, pad + 1, "emi.property.condition.predicate.nbt", predicate.getNbt());
            addEntityFlagsPredicate(components, pad + 1, translatable("emi.property.condition.predicate.flags"), predicate.getFlags());
            addEntityEquipmentPredicate(components, pad + 1, translatable("emi.property.condition.predicate.equipment"), predicate.getEquipment());
            addEntitySubPredicate(components, pad + 1, "emi.property.condition.predicate.entity_sub_type", predicate.getSubPredicate());
            addEntityPredicate(components, pad + 1, translatable("emi.property.condition.predicate.vehicle"), predicate.getVehicle());
            addEntityPredicate(components, pad + 1, translatable("emi.property.condition.predicate.passenger"), predicate.getPassenger());
            addEntityPredicate(components, pad + 1, translatable("emi.property.condition.predicate.targeted_entity"), predicate.getTargetedEntity());

            if (predicate.getTeam() != null) {
                components.add(pad(pad + 1, translatable("emi.property.condition.predicate.team", predicate.getTeam())));
            }
        }
    }

    private static void addEntityTypePredicate(List<Component> components, int pad, Component component, EntityTypePredicate entityTypePredicate) {
        if (entityTypePredicate != EntityTypePredicate.ANY) {
            MixinEntityTypePredicate predicate = (MixinEntityTypePredicate) entityTypePredicate;

            components.add(pad(pad, component));

            if (predicate instanceof MixinEntityTypePredicate.TypePredicate typePredicate) {
                components.add(pad(pad + 1, value(typePredicate.getType().getDescription())));
            }
            if (predicate instanceof MixinEntityTypePredicate.TagPredicate tagPredicate) {
                components.add(pad(pad + 1, value(tagPredicate.getTag().location()))); //TODO to list
            }
        }
    }

    private static void addDistancePredicate(List<Component> components, int pad, Component component, DistancePredicate distancePredicate) {
        if (distancePredicate != DistancePredicate.ANY) {
            MixinDistancePredicate predicate = (MixinDistancePredicate) distancePredicate;

            components.add(pad(pad, component));
            addMinMaxBounds(components, pad + 1, "emi.property.condition.dist_predicate.x", predicate.getX());
            addMinMaxBounds(components, pad + 1, "emi.property.condition.dist_predicate.y", predicate.getY());
            addMinMaxBounds(components, pad + 1, "emi.property.condition.dist_predicate.z", predicate.getZ());
            addMinMaxBounds(components, pad + 1, "emi.property.condition.dist_predicate.horizontal", predicate.getHorizontal());
            addMinMaxBounds(components, pad + 1, "emi.property.condition.dist_predicate.absolute", predicate.getAbsolute());
        }
    }

    private static void addDamageSourcePredicate(List<Component> components, int pad, Component component, DamageSourcePredicate damageSourcePredicate) {
        if (damageSourcePredicate != DamageSourcePredicate.ANY) {
            MixinDamageSourcePredicate predicate = (MixinDamageSourcePredicate) damageSourcePredicate;

            components.add(pad(pad, component));

            if (!predicate.getTags().isEmpty()) {
                components.add(pad(pad + 1, translatable("emi.property.condition.damage_source.tags")));
                predicate.getTags().forEach((tagPredicate) -> {
                    MixinTagPredicate<?> p = (MixinTagPredicate<?>) tagPredicate;

                    components.add(pad(pad + 2, keyValue(p.getTag().location().toString(), p.getExpected())));
                });
            }

            addEntityPredicate(components, pad + 1, translatable("emi.property.condition.damage_source.direct_entity"), predicate.getDirectEntity());
            addEntityPredicate(components, pad + 1, translatable("emi.property.condition.damage_source.source_entity"), predicate.getSourceEntity());
        }
    }

    private static void addLocationPredicate(List<Component> components, int pad, Component component, LocationPredicate predicate) {
        if (predicate != LocationPredicate.ANY) {
            MixinLocationPredicate locationPredicate = (MixinLocationPredicate) predicate;

            components.add(pad(pad, component));
            addMinMaxBounds(components, pad + 1, "emi.property.condition.location.x", locationPredicate.getX());
            addMinMaxBounds(components, pad + 1, "emi.property.condition.location.y", locationPredicate.getY());
            addMinMaxBounds(components, pad + 1, "emi.property.condition.location.z", locationPredicate.getZ());
            addResourceKey(components, pad + 1, "emi.property.condition.location.biome", locationPredicate.getBiome());
            addResourceKey(components, pad + 1, "emi.property.condition.location.structure", locationPredicate.getStructure());
            addResourceKey(components, pad + 1, "emi.property.condition.location.dimension", locationPredicate.getDimension());
            addBoolean(components, pad + 1, "emi.property.condition.location.smokey", locationPredicate.getSmokey());
            addLight(components, pad + 1, "emi.property.condition.location.light", locationPredicate.getLight());
            addBlock(components, pad + 1, translatable("emi.property.condition.location.block"), locationPredicate.getBlock());
            addFluid(components, pad + 1, translatable("emi.property.condition.location.fluid"), locationPredicate.getFluid());
        }
    }

    private static void addMobEffectsPredicate(List<Component> components, int pad, Component component, MobEffectsPredicate predicate) {
        if (predicate != MobEffectsPredicate.ANY) {
            MixinMobEffectPredicate locationPredicate = (MixinMobEffectPredicate) predicate;

            components.add(pad(pad, component));

            locationPredicate.getEffects().forEach((effect, instancePredicate) -> {
                MixinMobEffectPredicate.MobEffectInstancePredicate mobEffectInstancePredicate = (MixinMobEffectPredicate.MobEffectInstancePredicate) instancePredicate;

                components.add(pad(pad + 1, value(translatable(effect.getDescriptionId()))));
                addMinMaxBounds(components, pad + 2, "emi.property.condition.effect.amplifier", mobEffectInstancePredicate.getAmplifier());
                addMinMaxBounds(components, pad + 2, "emi.property.condition.effect.duration", mobEffectInstancePredicate.getDuration());
                addBoolean(components, pad + 2, "emi.property.condition.effect.ambient", mobEffectInstancePredicate.getAmbient());
                addBoolean(components, pad + 2, "emi.property.condition.effect.visible", mobEffectInstancePredicate.getVisible());
            });
        }
    }

    private static void addMinMaxBounds(List<Component> components, int pad, String key, MinMaxBounds.Doubles doubles) {
        if (doubles != MinMaxBounds.Doubles.ANY) {
            MixinMinMaxBounds.Doubles absolute = (MixinMinMaxBounds.Doubles) doubles;
            components.add(pad(pad, translatable(key, value(toString(absolute)))));
        }
    }

    private static void addMinMaxBounds(List<Component> components, int pad, String key, MinMaxBounds.Ints ints) {
        if (ints != MinMaxBounds.Ints.ANY) {
            MixinMinMaxBounds.Ints absolute = (MixinMinMaxBounds.Ints) ints;
            components.add(pad(pad, translatable(key, value(toString(absolute)))));
        }
    }

    private static void addResourceKey(List<Component> components, int pad, String key, @Nullable ResourceKey<?> resourceKey) {
        if (resourceKey != null) {
            components.add(pad(pad, translatable(key, value(resourceKey.location().toString()))));
        }
    }

    private static void addBoolean(List<Component> components, int pad, String key, @Nullable Boolean aBoolean) {
        if (aBoolean != null) {
            components.add(pad(pad, translatable(key, value(aBoolean.toString()))));
        }
    }

    private static void addLight(List<Component> components, int pad, String key, LightPredicate lightPredicate) {
        if (lightPredicate != LightPredicate.ANY) {
            addMinMaxBounds(components, pad, key, ((MixinLightPredicate) lightPredicate).getComposite());
        }
    }

    private static void addBlock(List<Component> components, int pad, Component component, BlockPredicate blockPredicate) {
        if (blockPredicate != BlockPredicate.ANY) {
            MixinBlockPredicate predicate = (MixinBlockPredicate) blockPredicate;

            components.add(pad(pad, component));

            if (predicate.getTag() != null) {
                components.add(pad(pad + 1, translatable("emi.property.condition.block.tag", predicate.getTag().location().toString())));
            }
            if (predicate.getBlocks() != null) {
                components.add(pad(pad + 1, translatable("emi.property.condition.block.blocks")));

                predicate.getBlocks().forEach((block) -> components.add(pad(pad + 2, value(translatable(block.getDescriptionId())))));
            }
            addStateProperties(components, pad + 1, translatable("emi.property.condition.block.state"), predicate.getProperties());
            addNbtPredicate(components, pad + 1, "emi.property.condition.block.nbt", predicate.getNbt());
        }
    }

    private static void addFluid(List<Component> components, int pad, Component component, FluidPredicate fluidPredicate) {
        if (fluidPredicate != FluidPredicate.ANY) {
            MixinFluidPredicate predicate = (MixinFluidPredicate) fluidPredicate;

            components.add(pad(pad, component));

            if (predicate.getTag() != null) {
                components.add(pad(pad + 1, translatable("emi.property.condition.fluid.tag", predicate.getTag().location().toString())));
            }
            if (predicate.getFluid() != null) {
                components.add(pad(pad + 1, translatable("emi.property.condition.fluid.fluid", value(translatable(predicate.getFluid().getFluidType().getDescriptionId())))));
            }
            addStateProperties(components, pad + 1, translatable("emi.property.condition.fluid.state"), predicate.getProperties());
        }
    }

    private static void addStateProperties(List<Component> components, int pad, Component component, StatePropertiesPredicate propertiesPredicate) {
        if (propertiesPredicate != StatePropertiesPredicate.ANY) {
            MixinStatePropertiesPredicate predicate = (MixinStatePropertiesPredicate) propertiesPredicate;

            components.add(pad(pad, component));

            predicate.getProperties().forEach((propertyMatcher) -> addPropertyMatcher(components, pad + 1, propertyMatcher));
        }
    }

    private static void addEntityFlagsPredicate(List<Component> components, int pad, Component component, EntityFlagsPredicate entityFlagsPredicate) {
        if (entityFlagsPredicate != EntityFlagsPredicate.ANY) {
            MixinEntityFlagsPredicate predicate = (MixinEntityFlagsPredicate) entityFlagsPredicate;

            components.add(pad(pad, component));

            addBoolean(components, pad + 1, "emi.property.condition.flags.on_fire", predicate.getIsOnFire());
            addBoolean(components, pad + 1, "emi.property.condition.flags.is_baby", predicate.getIsBaby());
            addBoolean(components, pad + 1, "emi.property.condition.flags.is_crouching", predicate.getIsCrouching());
            addBoolean(components, pad + 1, "emi.property.condition.flags.is_sprinting", predicate.getIsSprinting());
            addBoolean(components, pad + 1, "emi.property.condition.flags.is_swimming", predicate.getIsSwimming());
        }
    }

    private static void addEntityEquipmentPredicate(List<Component> components, int pad, Component component, EntityEquipmentPredicate entityEquipmentPredicate) {
        if (entityEquipmentPredicate != EntityEquipmentPredicate.ANY) {
            MixinEntityEquipmentPredicate predicate = (MixinEntityEquipmentPredicate) entityEquipmentPredicate;

            components.add(pad(pad, component));

            addItemPredicate(components, pad + 1, translatable("emi.property.condition.equipment.head"), predicate.getHead());
            addItemPredicate(components, pad + 1, translatable("emi.property.condition.equipment.chest"), predicate.getChest());
            addItemPredicate(components, pad + 1, translatable("emi.property.condition.equipment.legs"), predicate.getLegs());
            addItemPredicate(components, pad + 1, translatable("emi.property.condition.equipment.feet"), predicate.getFeet());
            addItemPredicate(components, pad + 1, translatable("emi.property.condition.equipment.mainhand"), predicate.getMainhand());
            addItemPredicate(components, pad + 1, translatable("emi.property.condition.equipment.offhand"), predicate.getOffhand());
        }
    }

    private static void addEntitySubPredicate(List<Component> components, int pad, String key, EntitySubPredicate entitySubPredicate) {
        if (entitySubPredicate != EntitySubPredicate.ANY) {
            Optional<Map.Entry<String, EntitySubPredicate.Type>> optional = EntitySubPredicate.Types.TYPES.entrySet().stream().filter((p) -> p.getValue() == entitySubPredicate.type()).findFirst();

            optional.ifPresent((entry) -> {
                components.add(pad(pad, translatable(key, entry.getKey())));

                if (entitySubPredicate instanceof LighthingBoltPredicate predicate) {
                    MixinLighthingBoltPredicate boltPredicate = (MixinLighthingBoltPredicate) predicate;

                    addMinMaxBounds(components, pad + 1, "emi.property.condition.sub_entity.blocks_on_fire", boltPredicate.getBlocksSetOnFire());
                    addEntityPredicate(components, pad + 2, translatable("emi.property.condition.sub_entity.stuck_entity"), boltPredicate.getEntityStruck());
                } else if (entitySubPredicate instanceof FishingHookPredicate predicate) {
                    MixinFishingHookPredicate fishingHookPredicate = (MixinFishingHookPredicate) predicate;

                    components.add(pad(pad + 1, translatable("emi.property.condition.sub_entity.in_open_water", fishingHookPredicate.isInOpenWater())));
                } else if (entitySubPredicate instanceof PlayerPredicate predicate) {
                    MixinPlayerPredicate playerPredicate = (MixinPlayerPredicate) predicate;

                    addMinMaxBounds(components, pad + 1, "emi.property.condition.sub_entity.level", playerPredicate.getLevel());

                    if (playerPredicate.getGameType() != null) {
                        components.add(pad(pad + 1, translatable("emi.property.condition.sub_entity.game_type", value(playerPredicate.getGameType().getShortDisplayName()))));
                    }

                    if (!playerPredicate.getStats().isEmpty()) {
                        components.add(pad(pad + 1, translatable("emi.property.condition.sub_entity.stats")));
                        playerPredicate.getStats().forEach((stat, ints) -> {
                            Object value = stat.getValue();
                            components.add(pad(pad + 2, value instanceof Item ? translatable(((Item) value).getDescriptionId()) : value));
                            components.add(pad(pad + 3, keyValue(stat.getType().getDisplayName(), EmiBaseLoot.toString((MixinMinMaxBounds.Ints) ints))));
                        });
                    }

                    if (!playerPredicate.getRecipes().isEmpty()) {
                        components.add(pad(pad + 1, translatable("emi.property.condition.sub_entity.recipes")));
                        playerPredicate.getRecipes().forEach((recipe, required) -> components.add(pad(pad + 2, keyValue(recipe.toString(), required))));
                    }

                    if (!playerPredicate.getAdvancements().isEmpty()) {
                        components.add(pad(pad + 1, translatable("emi.property.condition.sub_entity.advancements")));
                        playerPredicate.getAdvancements().forEach((advancement, advancementPredicate) -> {
                            components.add(pad(pad + 2, advancement.toString()));

                            if (advancementPredicate instanceof PlayerPredicate.AdvancementDonePredicate donePredicate) {
                                components.add(pad(pad + 3, translatable("emi.property.condition.sub_entity.advancement.done", ((MixinPlayerPredicate.AdvancementDonePredicate) donePredicate).getState())));
                            } else if (advancementPredicate instanceof PlayerPredicate.AdvancementCriterionsPredicate criterionsPredicate) {
                                MixinPlayerPredicate.AdvancementCriterionsPredicate mixPredicate = (MixinPlayerPredicate.AdvancementCriterionsPredicate) criterionsPredicate;

                                mixPredicate.getCriterions().forEach((criterion, state) -> components.add(pad(pad + 3, keyValue(criterion, state))));
                            }
                        });
                    }
                } if (entitySubPredicate instanceof SlimePredicate slimePredicate) {
                    MixinSlimePredicate predicate = (MixinSlimePredicate) slimePredicate;

                    addMinMaxBounds(components, pad + 1, "emi.property.condition.sub_entity.size", predicate.getSize());
                } else {
                    JsonObject jsonObject = entitySubPredicate.serializeCustomData();

                    if (jsonObject.has("variant")) {
                        components.add(pad(pad + 1, translatable("emi.property.condition.sub_entity.variant", jsonObject.getAsJsonPrimitive("variant").getAsString())));
                    } else {
                        components.add(pad(pad + 1, translatable("emi.property.condition.sub_entity.variant", jsonObject.toString())));
                    }
                }
            });
        }
    }

    private static void addItemPredicate(List<Component> components, int pad, Component component, ItemPredicate itemPredicate) {
        if (itemPredicate != ItemPredicate.ANY) {
            MixinItemPredicate predicate = (MixinItemPredicate) itemPredicate;

            components.add(pad(pad, component));

            if (predicate.getTag() != null) {
                components.add(pad(pad + 1, translatable("emi.property.condition.item.tag", predicate.getTag().location().toString())));
            }

            if (predicate.getItems() != null) {
                components.add(pad(pad + 1, translatable("emi.property.condition.item.items")));

                predicate.getItems().forEach((item) -> components.add(pad(pad + 2, value(translatable(item.getDescriptionId())))));
            }

            addMinMaxBounds(components, pad + 1, "emi.property.condition.item.count", predicate.getCount());
            addMinMaxBounds(components, pad + 1, "emi.property.condition.item.durability", predicate.getDurability());

            for (EnchantmentPredicate enchantment : predicate.getEnchantments()) {
                addEnchantmentPredicate(components, pad + 1, "emi.property.condition.item.enchantment", enchantment);
            }

            for (EnchantmentPredicate enchantment : predicate.getStoredEnchantments()) {
                addEnchantmentPredicate(components, pad + 1, "emi.property.condition.item.stored_enchantment", enchantment);
            }

            if (predicate.getPotion() != null) {
                components.add(pad(pad + 1, translatable("emi.property.condition.item.potion")));

                predicate.getPotion().getEffects().forEach((effect) -> components.add(pad(pad + 2, value(translatable(effect.getDescriptionId())))));
            }

            addNbtPredicate(components, pad + 1, "emi.property.condition.item.nbt", predicate.getNbt());
        }
    }

    private static void addEnchantmentPredicate(List<Component> components, int pad, String key, EnchantmentPredicate enchantmentPredicate) {
        if (enchantmentPredicate != EnchantmentPredicate.ANY) {
            MixinEnchantmentPredicate predicate = (MixinEnchantmentPredicate) enchantmentPredicate;

            components.add(pad(pad, translatable(key)));

            if (predicate.getEnchantment() != null) {
                components.add(pad(pad + 1, value(translatable(predicate.getEnchantment().getDescriptionId()))));
            }
            addMinMaxBounds(components, pad + 1, "emi.property.condition.enchantment.level", predicate.getLevel());
        }
    }

    private static void addNbtPredicate(List<Component> components, int pad, String key, NbtPredicate nbtPredicate) {
        if (nbtPredicate != NbtPredicate.ANY) {
            components.add(pad(pad, translatable(key, value(((MixinNbtPredicate) nbtPredicate).getTag().toString()))));
        }
    }

    private static void addPropertyMatcher(List<Component> components, int pad, StatePropertiesPredicate.PropertyMatcher propertyMatcher) {
        if (propertyMatcher instanceof MixinStatePropertiesPredicate.ExactPropertyMatcher matcher) {
            components.add(pad(pad, keyValue(matcher.getName(), matcher.getValue())));
        }
        if (propertyMatcher instanceof MixinStatePropertiesPredicate.RangedPropertyMatcher matcher) {
            components.add(pad(pad, value(matcher.getName() + "=[" + matcher.getMinValue() + "-" + matcher.getMaxValue() + "]")));
        }
    }

    @NotNull
    private static String toString(MixinMinMaxBounds.Doubles doubles) {
        if (doubles.getMin() != null) {
            if (doubles.getMax() != null) {
                if (!Objects.equals(doubles.getMin(), doubles.getMax())) {
                    return String.format("%.1f-%.1f", doubles.getMin(), doubles.getMax());
                } else {
                    return String.format("=%.1f", doubles.getMin());
                }
            } else {
                return String.format("≥%.1f", doubles.getMin());
            }
        } else {
            if (doubles.getMax() != null) {
                return String.format("<%.1f", doubles.getMax());
            }

            return "???";
        }
    }

    @NotNull
    private static String toString(MixinMinMaxBounds.Ints ints) {
        if (ints.getMin() != null) {
            if (ints.getMax() != null) {
                if (!Objects.equals(ints.getMin(), ints.getMax())) {
                    return String.format("%d-%d", ints.getMin(), ints.getMax());
                } else {
                    return String.format("=%d", ints.getMin());
                }
            } else {
                return String.format("≥%d", ints.getMin());
            }
        } else {
            if (ints.getMax() != null) {
                return String.format("<%d", ints.getMax());
            }

            return "???";
        }
    }
}
