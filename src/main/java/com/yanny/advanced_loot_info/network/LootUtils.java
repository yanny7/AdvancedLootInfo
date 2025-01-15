package com.yanny.advanced_loot_info.network;

import com.mojang.logging.LogUtils;
import com.yanny.advanced_loot_info.mixin.*;
import com.yanny.advanced_loot_info.network.condition.*;
import com.yanny.advanced_loot_info.network.function.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.entries.*;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

public class LootUtils {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Map<FunctionType, BiFunction<LootContext, LootItemFunction, LootFunction>> FUNCTION_MAP = new HashMap<>();
    public static final Map<FunctionType, BiFunction<FunctionType, FriendlyByteBuf, LootFunction>> FUNCTION_DECODE_MAP = new HashMap<>();
    public static final Map<ConditionType, BiFunction<LootContext, LootItemCondition, LootCondition>> CONDITION_MAP = new HashMap<>();
    public static final Map<ConditionType, BiFunction<ConditionType, FriendlyByteBuf, LootCondition>> CONDITION_DECODE_MAP = new HashMap<>();

    static {
        FUNCTION_MAP.put(FunctionType.SET_COUNT, SetCountFunction::new);
        FUNCTION_MAP.put(FunctionType.ENCHANT_WITH_LEVELS, EnchantWithLevelsFunction::new);
        FUNCTION_MAP.put(FunctionType.ENCHANT_RANDOMLY, EnchantRandomlyFunction::new);
        FUNCTION_MAP.put(FunctionType.SET_ENCHANTMENTS, SetEnchantmentsFunction::new);
        FUNCTION_MAP.put(FunctionType.SET_NBT, SetNbtFunction::new);
        FUNCTION_MAP.put(FunctionType.FURNACE_SMELT, FurnaceSmeltFunction::new);
        FUNCTION_MAP.put(FunctionType.LOOTING_ENCHANT, LootingEnchantFunction::new);
        FUNCTION_MAP.put(FunctionType.SET_DAMAGE, SetDamageFunction::new);
        FUNCTION_MAP.put(FunctionType.SET_ATTRIBUTES, SetAttributesFunction::new);
        FUNCTION_MAP.put(FunctionType.SET_NAME, SetNameFunction::new);
        FUNCTION_MAP.put(FunctionType.EXPLORATION_MAP, ExplorationMapFunction::new);
        FUNCTION_MAP.put(FunctionType.SET_STEW_EFFECT, SetStewEffectFunction::new);
        FUNCTION_MAP.put(FunctionType.COPY_NAME, CopyNameFunction::new);
        FUNCTION_MAP.put(FunctionType.SET_CONTENTS, SetContentsFunction::new);
        FUNCTION_MAP.put(FunctionType.LIMIT_COUNT, LimitCountFunction::new);
        FUNCTION_MAP.put(FunctionType.APPLY_BONUS, ApplyBonusFunction::new);
        FUNCTION_MAP.put(FunctionType.SET_LOOT_TABLE, SetLootTableFunction::new);
        FUNCTION_MAP.put(FunctionType.EXPLOSION_DECAY, ExplosionDecayFunction::new);
        FUNCTION_MAP.put(FunctionType.SET_LORE, SetLoreFunction::new);
        FUNCTION_MAP.put(FunctionType.FILL_PLAYER_HEAD, FillPlayerHeadFunction::new);
        FUNCTION_MAP.put(FunctionType.COPY_NBT, CopyNbtFunction::new);
        FUNCTION_MAP.put(FunctionType.COPY_STATE, CopyStateFunction::new);
        FUNCTION_MAP.put(FunctionType.SET_BANNER_PATTERN, SetBannerPatternFunction::new);
        FUNCTION_MAP.put(FunctionType.SET_POTION, SetPotionFunction::new);
        FUNCTION_MAP.put(FunctionType.SET_INSTRUMENT, SetInstrumentFunction::new);
        FUNCTION_MAP.put(FunctionType.REFERENCE, ReferenceFunction::new);
        FUNCTION_MAP.put(FunctionType.UNKNOWN, UnknownFunction::new);

        FUNCTION_DECODE_MAP.put(FunctionType.SET_COUNT, SetCountFunction::new);
        FUNCTION_DECODE_MAP.put(FunctionType.ENCHANT_WITH_LEVELS, EnchantWithLevelsFunction::new);
        FUNCTION_DECODE_MAP.put(FunctionType.ENCHANT_RANDOMLY, EnchantRandomlyFunction::new);
        FUNCTION_DECODE_MAP.put(FunctionType.SET_ENCHANTMENTS, SetEnchantmentsFunction::new);
        FUNCTION_DECODE_MAP.put(FunctionType.SET_NBT, SetNbtFunction::new);
        FUNCTION_DECODE_MAP.put(FunctionType.FURNACE_SMELT, FurnaceSmeltFunction::new);
        FUNCTION_DECODE_MAP.put(FunctionType.LOOTING_ENCHANT, LootingEnchantFunction::new);
        FUNCTION_DECODE_MAP.put(FunctionType.SET_DAMAGE, SetDamageFunction::new);
        FUNCTION_DECODE_MAP.put(FunctionType.SET_ATTRIBUTES, SetAttributesFunction::new);
        FUNCTION_DECODE_MAP.put(FunctionType.SET_NAME, SetNameFunction::new);
        FUNCTION_DECODE_MAP.put(FunctionType.EXPLORATION_MAP, ExplorationMapFunction::new);
        FUNCTION_DECODE_MAP.put(FunctionType.SET_STEW_EFFECT, SetStewEffectFunction::new);
        FUNCTION_DECODE_MAP.put(FunctionType.COPY_NAME, CopyNameFunction::new);
        FUNCTION_DECODE_MAP.put(FunctionType.SET_CONTENTS, SetContentsFunction::new);
        FUNCTION_DECODE_MAP.put(FunctionType.LIMIT_COUNT, LimitCountFunction::new);
        FUNCTION_DECODE_MAP.put(FunctionType.APPLY_BONUS, ApplyBonusFunction::new);
        FUNCTION_DECODE_MAP.put(FunctionType.SET_LOOT_TABLE, SetLootTableFunction::new);
        FUNCTION_DECODE_MAP.put(FunctionType.EXPLOSION_DECAY, ExplosionDecayFunction::new);
        FUNCTION_DECODE_MAP.put(FunctionType.SET_LORE, SetLoreFunction::new);
        FUNCTION_DECODE_MAP.put(FunctionType.FILL_PLAYER_HEAD, FillPlayerHeadFunction::new);
        FUNCTION_DECODE_MAP.put(FunctionType.COPY_NBT, CopyNbtFunction::new);
        FUNCTION_DECODE_MAP.put(FunctionType.COPY_STATE, CopyStateFunction::new);
        FUNCTION_DECODE_MAP.put(FunctionType.SET_BANNER_PATTERN, SetBannerPatternFunction::new);
        FUNCTION_DECODE_MAP.put(FunctionType.SET_POTION, SetPotionFunction::new);
        FUNCTION_DECODE_MAP.put(FunctionType.SET_INSTRUMENT, SetInstrumentFunction::new);
        FUNCTION_DECODE_MAP.put(FunctionType.REFERENCE, ReferenceFunction::new);
        FUNCTION_DECODE_MAP.put(FunctionType.UNKNOWN, UnknownFunction::new);

        CONDITION_MAP.put(ConditionType.INVERTED, InvertedCondition::new);
        CONDITION_MAP.put(ConditionType.ANY_OF, AnyOfCondition::new);
        CONDITION_MAP.put(ConditionType.ALL_OF, AllOfCondition::new);
        CONDITION_MAP.put(ConditionType.RANDOM_CHANCE, RandomChanceCondition::new);
        CONDITION_MAP.put(ConditionType.RANDOM_CHANCE_WITH_LOOTING, RandomChanceWithLootingCondition::new);
        CONDITION_MAP.put(ConditionType.ENTITY_PROPERTIES, EntityPropertiesCondition::new);
        CONDITION_MAP.put(ConditionType.KILLED_BY_PLAYER, KilledByPlayerCondition::new);
        CONDITION_MAP.put(ConditionType.ENTITY_SCORES, EntityScoresCondition::new);
        CONDITION_MAP.put(ConditionType.BLOCK_STATE_PROPERTY, BlockStatePropertyCondition::new);
        CONDITION_MAP.put(ConditionType.MATCH_TOOL, MatchToolCondition::new);
        CONDITION_MAP.put(ConditionType.TABLE_BONUS, TableBonusCondition::new);
        CONDITION_MAP.put(ConditionType.SURVIVES_EXPLOSION, SurvivesExplosionCondition::new);
        CONDITION_MAP.put(ConditionType.DAMAGE_SOURCE_PROPERTIES, DamageSourcePropertiesCondition::new);
        CONDITION_MAP.put(ConditionType.LOCATION_CHECK, LocationCheckCondition::new);
        CONDITION_MAP.put(ConditionType.WEATHER_CHECK, WeatherCheckCondition::new);
        CONDITION_MAP.put(ConditionType.REFERENCE, ReferenceCondition::new);
        CONDITION_MAP.put(ConditionType.TIME_CHECK, TimeCheckCondition::new);
        CONDITION_MAP.put(ConditionType.VALUE_CHECK, ValueCheckCondition::new);
        CONDITION_MAP.put(ConditionType.LOOT_CONDITION_TYPE, CanToolPerformActionCondition::new);
        CONDITION_MAP.put(ConditionType.UNKNOWN, UnknownCondition::new);

        CONDITION_DECODE_MAP.put(ConditionType.INVERTED, InvertedCondition::new);
        CONDITION_DECODE_MAP.put(ConditionType.ANY_OF, AnyOfCondition::new);
        CONDITION_DECODE_MAP.put(ConditionType.ALL_OF, AllOfCondition::new);
        CONDITION_DECODE_MAP.put(ConditionType.RANDOM_CHANCE, RandomChanceCondition::new);
        CONDITION_DECODE_MAP.put(ConditionType.RANDOM_CHANCE_WITH_LOOTING, RandomChanceWithLootingCondition::new);
        CONDITION_DECODE_MAP.put(ConditionType.ENTITY_PROPERTIES, EntityPropertiesCondition::new);
        CONDITION_DECODE_MAP.put(ConditionType.KILLED_BY_PLAYER, KilledByPlayerCondition::new);
        CONDITION_DECODE_MAP.put(ConditionType.ENTITY_SCORES, EntityScoresCondition::new);
        CONDITION_DECODE_MAP.put(ConditionType.BLOCK_STATE_PROPERTY, BlockStatePropertyCondition::new);
        CONDITION_DECODE_MAP.put(ConditionType.MATCH_TOOL, MatchToolCondition::new);
        CONDITION_DECODE_MAP.put(ConditionType.TABLE_BONUS, TableBonusCondition::new);
        CONDITION_DECODE_MAP.put(ConditionType.SURVIVES_EXPLOSION, SurvivesExplosionCondition::new);
        CONDITION_DECODE_MAP.put(ConditionType.DAMAGE_SOURCE_PROPERTIES, DamageSourcePropertiesCondition::new);
        CONDITION_DECODE_MAP.put(ConditionType.LOCATION_CHECK, LocationCheckCondition::new);
        CONDITION_DECODE_MAP.put(ConditionType.WEATHER_CHECK, WeatherCheckCondition::new);
        CONDITION_DECODE_MAP.put(ConditionType.REFERENCE, ReferenceCondition::new);
        CONDITION_DECODE_MAP.put(ConditionType.TIME_CHECK, TimeCheckCondition::new);
        CONDITION_DECODE_MAP.put(ConditionType.VALUE_CHECK, ValueCheckCondition::new);
        CONDITION_DECODE_MAP.put(ConditionType.LOOT_CONDITION_TYPE, CanToolPerformActionCondition::new);
        CONDITION_DECODE_MAP.put(ConditionType.UNKNOWN, UnknownCondition::new);
    }

    @NotNull
    public static LootTableEntry parseLoot(LootTable table, LootDataManager manager, LootContext lootContext, List<Item> items, float chance) {
        MixinLootTable lootTable = (MixinLootTable) table;
        List<LootPoolEntry> lootInfos = new LinkedList<>();
        List<LootPool> pools = lootTable.getPools();

        boolean wasSmelting = Arrays.stream(lootTable.getFunctions()).anyMatch((f) -> f.getType() == LootItemFunctions.FURNACE_SMELT);

        pools.forEach((pool) -> lootInfos.add(parsePool(pool, manager, lootContext, items, chance, wasSmelting)));
        return new LootTableEntry(lootInfos, LootFunction.of(lootContext, lootTable.getFunctions()));
    }

    @NotNull
    private static LootPoolEntry parsePool(LootPool pool, LootDataManager manager, LootContext context, List<Item> items, float chance, boolean wasSmelting) {
        MixinLootPool mixinLootPool = (MixinLootPool) pool;
        RangeValue rolls = RangeValue.of(context, mixinLootPool.getRolls());
        RangeValue bonusRolls = RangeValue.of(context, mixinLootPool.getBonusRolls());
        List<LootFunction> functions = LootFunction.of(context, mixinLootPool.getFunctions());
        List<LootCondition> conditions = LootCondition.of(context, mixinLootPool.getConditions());

        wasSmelting |= functions.stream().anyMatch((f) -> f.type == FunctionType.FURNACE_SMELT);

        return new LootPoolEntry(parseEntries(mixinLootPool.getEntries(), manager, context, items, chance, true, wasSmelting), rolls, bonusRolls, functions, conditions);
    }

    @NotNull
    private static List<LootEntry> parseEntries(LootPoolEntryContainer[] entries, LootDataManager manager, LootContext lootContext, List<Item> items, float chance, boolean weighted, boolean wasSmelting) {
        List<LootEntry> lootInfos = new LinkedList<>();
        int sumWeight = Arrays.stream(entries).filter(entry -> entry instanceof LootPoolSingletonContainer).mapToInt(entry -> ((MixinLootPoolSingletonContainer) entry).getWeight()).sum();

        Function<Integer, Float> getChance = (weight) -> {
            if (weighted) {
                return chance * (weight / (float) sumWeight);
            } else {
                return chance;
            }
        };

        for (LootPoolEntryContainer entry : entries) {
            LootPoolEntryType type = entry.getType();

            //noinspection StatementWithEmptyBody
            if (type == LootPoolEntries.EMPTY) {
                // Skip empty entry
            } else if (type == LootPoolEntries.REFERENCE) {
                ResourceLocation location = ((MixinLootTableReference) entry).getName();
                LootTable table = manager.getElement(LootDataType.TABLE, location);
                int weight = ((MixinLootPoolSingletonContainer) entry).getWeight();

                if (table != null) {
                    lootInfos.add(parseLoot(table, manager, lootContext, items, getChance.apply(weight)));
                } else {
                    LOGGER.warn("Invalid LootTable reference {}", location);
                }
            } else if (type == LootPoolEntries.DYNAMIC) {
                LOGGER.warn("Unimplemented dynamic loot entry, skipping");
            } else if (type == LootPoolEntries.TAG) {
                int weight = ((MixinLootPoolSingletonContainer) entry).getWeight();

                lootInfos.addAll(parseTagEntry((TagEntry) entry, lootContext, items, getChance.apply(weight), wasSmelting));
            } else if (type == LootPoolEntries.GROUP) {
                lootInfos.add(new LootGroup(GroupType.GROUP, parseEntries(((MixinCompositeEntryBase) entry).getChildren(), manager, lootContext, items, chance, false, wasSmelting), List.of(), List.of()));
            } else if (type == LootPoolEntries.SEQUENCE) {
                lootInfos.add(new LootGroup(GroupType.SEQUENCE, parseEntries(((MixinCompositeEntryBase) entry).getChildren(), manager, lootContext, items, chance, false, wasSmelting), List.of(), List.of()));
            } else if (type == LootPoolEntries.ALTERNATIVES) {
                lootInfos.add(new LootGroup(GroupType.ALTERNATIVES, parseEntries(((MixinCompositeEntryBase) entry).getChildren(), manager, lootContext, items, chance, false, wasSmelting), List.of(), List.of()));
            } else if (type == LootPoolEntries.ITEM) {
                int weight = ((MixinLootPoolSingletonContainer) entry).getWeight();

                lootInfos.addAll(parseLootItem((LootItem) entry, lootContext, items, getChance.apply(weight), wasSmelting));
            } else {
                LOGGER.warn("Unknown LootPool entry type {}", type);
            }
        }

        return lootInfos;
    }

    @NotNull
    private static List<LootEntry> parseLootItem(LootItem lootItem, LootContext lootContext, List<Item> items, float chance, boolean wasSmelting) {
        List<LootEntry> lootInfos = new LinkedList<>();
        Item item = ((MixinLootItem) lootItem).getItem();
        Tuple<LootItemFunction[], LootItemCondition[]> tuple = handleSmeltingFunction(lootItem, lootContext, items, lootInfos, item, chance, wasSmelting);

        if (!wasSmelting) {
            items.add(item);
            lootInfos.add(new LootInfo(
                    ForgeRegistries.ITEMS.getKey(item),
                    LootFunction.of(lootContext, tuple.getA()),
                    LootCondition.of(lootContext, tuple.getB()),
                    chance
            ));
        }

        return lootInfos;
    }

    @NotNull
    private static List<LootEntry> parseTagEntry(TagEntry entry, LootContext lootContext, List<Item> items, float chance, boolean wasSmelting) {
        List<LootEntry> lootInfos = new LinkedList<>();
        ITagManager<Item> tags = ForgeRegistries.ITEMS.tags();

        if (tags != null) {
            for (Item item : tags.getTag(((MixinTagEntry) entry).getTag())) {
                Tuple<LootItemFunction[], LootItemCondition[]> tuple = handleSmeltingFunction(entry, lootContext, items, lootInfos, item, chance, wasSmelting);

                if (!wasSmelting) {
                    items.add(item);
                    lootInfos.add(new LootInfo(
                            ForgeRegistries.ITEMS.getKey(item),
                            LootFunction.of(lootContext, tuple.getA()),
                            LootCondition.of(lootContext, tuple.getB()),
                            chance
                    ));
                }
            }
        }

        return lootInfos;
    }

    @NotNull
    private static Tuple<LootItemFunction[], LootItemCondition[]> handleSmeltingFunction(LootPoolSingletonContainer container, LootContext lootContext, List<Item> items,
                                                                                         List<LootEntry> lootInfos, Item item, float chance, boolean wasSmelting) {
        LootItemFunction[] functions = ((MixinLootPoolSingletonContainer) container).getFunctions();
        LootItemCondition[] conditions = ((MixinLootPoolEntryContainer) container).getConditions();
        Optional<LootItemFunction> optional = Arrays.stream(functions).filter((f) -> f.getType() == LootItemFunctions.FURNACE_SMELT).findAny();

        if (optional.isPresent() || wasSmelting) {
            Optional<SmeltingRecipe> optionalSmeltingRecipe = lootContext.getLevel().getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SimpleContainer(new ItemStack(item)), lootContext.getLevel());

            if (optionalSmeltingRecipe.isPresent()) {
                SmeltingRecipe recipe = optionalSmeltingRecipe.get();
                Item smeltItem = recipe.getResultItem(null).getItem();

                items.add(smeltItem);

                if (!wasSmelting) {
                    LootItemCondition[] smeltConditions = ((MixinLootItemConditionalFunction) optional.get()).getPredicates();

                    functions = Arrays.stream(functions).filter((f) -> f.getType() != LootItemFunctions.FURNACE_SMELT).toArray(LootItemFunction[]::new);
                    lootInfos.add(new LootInfo(
                            ForgeRegistries.ITEMS.getKey(smeltItem),
                            LootFunction.of(lootContext, functions),
                            LootCondition.of(lootContext, Stream.concat(Arrays.stream(conditions), Arrays.stream(smeltConditions)).toArray(LootItemCondition[]::new)),
                            chance
                    ));
                    conditions = Stream.concat(Arrays.stream(conditions), Arrays.stream(
                            new LootItemCondition[]{new InvertedLootItemCondition(new net.minecraft.world.level.storage.loot.predicates.AllOfCondition(smeltConditions))}
                    )).toArray(LootItemCondition[]::new);
                } else {
                    lootInfos.add(new LootInfo(
                            ForgeRegistries.ITEMS.getKey(smeltItem),
                            LootFunction.of(lootContext, functions),
                            LootCondition.of(lootContext, conditions),
                            chance
                    ));
                }
            }
        }

        return new Tuple<>(functions, conditions);
    }
}
