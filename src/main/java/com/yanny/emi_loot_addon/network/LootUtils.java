package com.yanny.emi_loot_addon.network;

import com.mojang.logging.LogUtils;
import com.yanny.emi_loot_addon.mixin.*;
import com.yanny.emi_loot_addon.network.condition.*;
import com.yanny.emi_loot_addon.network.function.*;
import com.yanny.emi_loot_addon.network.value.RangeValue;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.entries.*;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;
import org.slf4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

@ParametersAreNonnullByDefault
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
        CONDITION_DECODE_MAP.put(ConditionType.UNKNOWN, UnknownCondition::new);
    }

    public static LootGroup parseLoot(LootTable table, LootDataManager manager, LootContext lootContext, float chance) {
        MixinLootTable lootTable = (MixinLootTable) table;
        List<LootEntry> lootInfos = new LinkedList<>();
        List<LootPool> pools = lootTable.getPools();

        pools.forEach((pool) -> lootInfos.add(parsePool(pool, manager, lootContext, chance)));
        return new LootGroup(lootInfos, LootFunction.of(lootContext, lootTable.getFunctions()), List.of());
    }

    private static LootEntry parsePool(LootPool pool, LootDataManager manager, LootContext context, float chance) {
        MixinLootPool mixinLootPool = (MixinLootPool) pool;
        RangeValue rolls = RangeValue.of(context, mixinLootPool.getRolls());
        RangeValue bonusRolls = RangeValue.of(context, mixinLootPool.getBonusRolls());
        List<LootFunction> functions = LootFunction.of(context, mixinLootPool.getFunctions());
        List<LootCondition> conditions = LootCondition.of(context, mixinLootPool.getConditions());
        return new LootPoolEntry(parseEntries(mixinLootPool.getEntries(), manager, context, chance, true), rolls, bonusRolls, functions, conditions);
    }

    private static List<LootEntry> parseEntries(LootPoolEntryContainer[] entries, LootDataManager manager, LootContext lootContext, float chance, boolean weighted) {
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
                    lootInfos.add(parseLoot(table, manager, lootContext, getChance.apply(weight)));
                } else {
                    LOGGER.warn("Invalid LootTable reference {}", location);
                }
            } else if (type == LootPoolEntries.DYNAMIC) {
                LOGGER.warn("Unimplemented dynamic loot entry, skipping");
            } else if (type == LootPoolEntries.TAG) {
                int weight = ((MixinLootPoolSingletonContainer) entry).getWeight();

                lootInfos.addAll(parseTagEntry((TagEntry) entry, lootContext, getChance.apply(weight)));
//                lootInfos.addAll(parseTagEntry((TagEntry) entry, chance * (weight / (float)sumWeight)));
            } else if (type == LootPoolEntries.ALTERNATIVES || type == LootPoolEntries.SEQUENCE || type == LootPoolEntries.GROUP) {
                lootInfos.add(new LootGroup(parseEntries(((MixinCompositeEntryBase) entry).getChildren(), manager, lootContext, chance, false), List.of(), List.of()));
            } else if (type == LootPoolEntries.ITEM) {
                int weight = ((MixinLootPoolSingletonContainer) entry).getWeight();

                lootInfos.add(parseLootItem((LootItem) entry, lootContext, getChance.apply(weight)));
            } else {
                LOGGER.warn("Unknown LootPool entry type {}", type);
            }
        }

        return lootInfos;
    }

    private static LootInfo parseLootItem(LootItem lootItem, LootContext lootContext, float chance) {
        return new LootInfo(
                ForgeRegistries.ITEMS.getKey(((MixinLootItem) lootItem).getItem()),
                LootFunction.of(lootContext, ((MixinLootPoolSingletonContainer) lootItem).getFunctions()),
                LootCondition.of(lootContext, ((MixinLootPoolEntryContainer) lootItem).getConditions()),
                chance
        );
    }

    private static List<LootEntry> parseTagEntry(TagEntry entry, LootContext lootContext, float chance) {
        ITagManager<Item> tags = ForgeRegistries.ITEMS.tags();

        if (tags != null) {
            return tags.getTag(((MixinTagEntry) entry).getTag()).stream().map((item) -> {
                ResourceLocation location = ForgeRegistries.ITEMS.getKey(item);
                return new LootInfo(
                        location,
                        LootFunction.of(lootContext, ((MixinLootPoolSingletonContainer) entry).getFunctions()),
                        LootCondition.of(lootContext, ((MixinLootPoolEntryContainer) entry).getConditions()),
                        chance
                );
            }).collect(Collectors.toList());
        }

        return List.of();
    }
}
