package com.yanny.advanced_loot_info.plugin;

import com.yanny.advanced_loot_info.api.AliEntrypoint;
import com.yanny.advanced_loot_info.api.IPlugin;
import com.yanny.advanced_loot_info.api.IRegistry;
import com.yanny.advanced_loot_info.api.UnknownFunction;
import com.yanny.advanced_loot_info.plugin.condition.*;
import com.yanny.advanced_loot_info.plugin.function.*;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import net.minecraftforge.common.loot.CanToolPerformAction;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@AliEntrypoint
public class VanillaPlugin implements IPlugin {
    public static final ResourceLocation UNKNOWN = new ResourceLocation("unknown");

    @Override
    public void register(IRegistry registry) {
        registry.registerFunction(ApplyBonusFunction.class, getKey(BuiltInRegistries.LOOT_FUNCTION_TYPE, LootItemFunctions.APPLY_BONUS), ApplyBonusFunction::new, ApplyBonusFunction::new);
        registry.registerFunction(CopyNameFunction.class, getKey(BuiltInRegistries.LOOT_FUNCTION_TYPE, LootItemFunctions.COPY_NAME), CopyNameFunction::new, CopyNameFunction::new);
        registry.registerFunction(CopyNbtFunction.class, getKey(BuiltInRegistries.LOOT_FUNCTION_TYPE, LootItemFunctions.COPY_NBT), CopyNbtFunction::new, CopyNbtFunction::new);
        registry.registerFunction(CopyStateFunction.class, getKey(BuiltInRegistries.LOOT_FUNCTION_TYPE, LootItemFunctions.COPY_STATE), CopyStateFunction::new, CopyStateFunction::new);
        registry.registerFunction(EnchantRandomlyFunction.class, getKey(BuiltInRegistries.LOOT_FUNCTION_TYPE, LootItemFunctions.ENCHANT_RANDOMLY), EnchantRandomlyFunction::new, EnchantRandomlyFunction::new);
        registry.registerFunction(EnchantWithLevelsFunction.class, getKey(BuiltInRegistries.LOOT_FUNCTION_TYPE, LootItemFunctions.ENCHANT_WITH_LEVELS), EnchantWithLevelsFunction::new, EnchantWithLevelsFunction::new);
        registry.registerFunction(ExplorationMapFunction.class, getKey(BuiltInRegistries.LOOT_FUNCTION_TYPE, LootItemFunctions.EXPLORATION_MAP), ExplorationMapFunction::new, ExplorationMapFunction::new);
        registry.registerFunction(ExplosionDecayFunction.class, getKey(BuiltInRegistries.LOOT_FUNCTION_TYPE, LootItemFunctions.EXPLOSION_DECAY), ExplosionDecayFunction::new, ExplosionDecayFunction::new);
        registry.registerFunction(FillPlayerHeadFunction.class, getKey(BuiltInRegistries.LOOT_FUNCTION_TYPE, LootItemFunctions.FILL_PLAYER_HEAD), FillPlayerHeadFunction::new, FillPlayerHeadFunction::new);
        registry.registerFunction(FurnaceSmeltFunction.class, getKey(BuiltInRegistries.LOOT_FUNCTION_TYPE, LootItemFunctions.FURNACE_SMELT), FurnaceSmeltFunction::new, FurnaceSmeltFunction::new);
        registry.registerFunction(LimitCountFunction.class, getKey(BuiltInRegistries.LOOT_FUNCTION_TYPE, LootItemFunctions.LIMIT_COUNT), LimitCountFunction::new, LimitCountFunction::new);
        registry.registerFunction(LootingEnchantFunction.class, getKey(BuiltInRegistries.LOOT_FUNCTION_TYPE, LootItemFunctions.LOOTING_ENCHANT), LootingEnchantFunction::new, LootingEnchantFunction::new);
        registry.registerFunction(ReferenceFunction.class, getKey(BuiltInRegistries.LOOT_FUNCTION_TYPE, LootItemFunctions.REFERENCE), ReferenceFunction::new, ReferenceFunction::new);
        registry.registerFunction(SetAttributesFunction.class, getKey(BuiltInRegistries.LOOT_FUNCTION_TYPE, LootItemFunctions.SET_ATTRIBUTES), SetAttributesFunction::new, SetAttributesFunction::new);
        registry.registerFunction(SetBannerPatternFunction.class, getKey(BuiltInRegistries.LOOT_FUNCTION_TYPE, LootItemFunctions.SET_BANNER_PATTERN), SetBannerPatternFunction::new, SetBannerPatternFunction::new);
        registry.registerFunction(SetContentsFunction.class, getKey(BuiltInRegistries.LOOT_FUNCTION_TYPE, LootItemFunctions.SET_CONTENTS), SetContentsFunction::new, SetContentsFunction::new);
        registry.registerFunction(SetCountFunction.class, getKey(BuiltInRegistries.LOOT_FUNCTION_TYPE, LootItemFunctions.SET_COUNT), SetCountFunction::new, SetCountFunction::new);
        registry.registerFunction(SetDamageFunction.class, getKey(BuiltInRegistries.LOOT_FUNCTION_TYPE, LootItemFunctions.SET_DAMAGE), SetDamageFunction::new, SetDamageFunction::new);
        registry.registerFunction(SetEnchantmentsFunction.class, getKey(BuiltInRegistries.LOOT_FUNCTION_TYPE, LootItemFunctions.SET_ENCHANTMENTS), SetEnchantmentsFunction::new, SetEnchantmentsFunction::new);
        registry.registerFunction(SetInstrumentFunction.class, getKey(BuiltInRegistries.LOOT_FUNCTION_TYPE, LootItemFunctions.SET_INSTRUMENT), SetInstrumentFunction::new, SetInstrumentFunction::new);
        registry.registerFunction(SetLootTableFunction.class, getKey(BuiltInRegistries.LOOT_FUNCTION_TYPE, LootItemFunctions.SET_LOOT_TABLE), SetLootTableFunction::new, SetLootTableFunction::new);
        registry.registerFunction(SetLoreFunction.class, getKey(BuiltInRegistries.LOOT_FUNCTION_TYPE, LootItemFunctions.SET_LORE), SetLoreFunction::new, SetLoreFunction::new);
        registry.registerFunction(SetNameFunction.class, getKey(BuiltInRegistries.LOOT_FUNCTION_TYPE, LootItemFunctions.SET_NAME), SetNameFunction::new, SetNameFunction::new);
        registry.registerFunction(SetNbtFunction.class, getKey(BuiltInRegistries.LOOT_FUNCTION_TYPE, LootItemFunctions.SET_NBT), SetNbtFunction::new, SetNbtFunction::new);
        registry.registerFunction(SetPotionFunction.class, getKey(BuiltInRegistries.LOOT_FUNCTION_TYPE, LootItemFunctions.SET_POTION), SetPotionFunction::new, SetPotionFunction::new);
        registry.registerFunction(SetStewEffectFunction.class, getKey(BuiltInRegistries.LOOT_FUNCTION_TYPE, LootItemFunctions.SET_STEW_EFFECT), SetStewEffectFunction::new, SetStewEffectFunction::new);
        registry.registerFunction(UnknownFunction.class, UNKNOWN, UnknownFunction::new, UnknownFunction::new);

        registry.registerCondition(AllOfCondition.class, getKey(BuiltInRegistries.LOOT_CONDITION_TYPE, LootItemConditions.ALL_OF), AllOfCondition::new, AllOfCondition::new);
        registry.registerCondition(AnyOfCondition.class, getKey(BuiltInRegistries.LOOT_CONDITION_TYPE, LootItemConditions.ANY_OF), AnyOfCondition::new, AnyOfCondition::new);
        registry.registerCondition(BlockStatePropertyCondition.class, getKey(BuiltInRegistries.LOOT_CONDITION_TYPE, LootItemConditions.BLOCK_STATE_PROPERTY), BlockStatePropertyCondition::new, BlockStatePropertyCondition::new);
        registry.registerCondition(CanToolPerformActionCondition.class, getKey(BuiltInRegistries.LOOT_CONDITION_TYPE, CanToolPerformAction.LOOT_CONDITION_TYPE), CanToolPerformActionCondition::new, CanToolPerformActionCondition::new);
        registry.registerCondition(DamageSourcePropertiesCondition.class, getKey(BuiltInRegistries.LOOT_CONDITION_TYPE, LootItemConditions.DAMAGE_SOURCE_PROPERTIES), DamageSourcePropertiesCondition::new, DamageSourcePropertiesCondition::new);
        registry.registerCondition(EntityPropertiesCondition.class, getKey(BuiltInRegistries.LOOT_CONDITION_TYPE, LootItemConditions.ENTITY_PROPERTIES), EntityPropertiesCondition::new, EntityPropertiesCondition::new);
        registry.registerCondition(EntityScoresCondition.class, getKey(BuiltInRegistries.LOOT_CONDITION_TYPE, LootItemConditions.ENTITY_SCORES), EntityScoresCondition::new, EntityScoresCondition::new);
        registry.registerCondition(InvertedCondition.class, getKey(BuiltInRegistries.LOOT_CONDITION_TYPE, LootItemConditions.INVERTED), InvertedCondition::new, InvertedCondition::new);
        registry.registerCondition(KilledByPlayerCondition.class, getKey(BuiltInRegistries.LOOT_CONDITION_TYPE, LootItemConditions.KILLED_BY_PLAYER), KilledByPlayerCondition::new, KilledByPlayerCondition::new);
        registry.registerCondition(LocationCheckCondition.class, getKey(BuiltInRegistries.LOOT_CONDITION_TYPE, LootItemConditions.LOCATION_CHECK), LocationCheckCondition::new, LocationCheckCondition::new);
        registry.registerCondition(MatchToolCondition.class, getKey(BuiltInRegistries.LOOT_CONDITION_TYPE, LootItemConditions.MATCH_TOOL), MatchToolCondition::new, MatchToolCondition::new);
        registry.registerCondition(RandomChanceCondition.class, getKey(BuiltInRegistries.LOOT_CONDITION_TYPE, LootItemConditions.RANDOM_CHANCE), RandomChanceCondition::new, RandomChanceCondition::new);
        registry.registerCondition(RandomChanceWithLootingCondition.class, getKey(BuiltInRegistries.LOOT_CONDITION_TYPE, LootItemConditions.RANDOM_CHANCE_WITH_LOOTING), RandomChanceWithLootingCondition::new, RandomChanceWithLootingCondition::new);
        registry.registerCondition(ReferenceCondition.class, getKey(BuiltInRegistries.LOOT_CONDITION_TYPE, LootItemConditions.REFERENCE), ReferenceCondition::new, ReferenceCondition::new);
        registry.registerCondition(SurvivesExplosionCondition.class, getKey(BuiltInRegistries.LOOT_CONDITION_TYPE, LootItemConditions.SURVIVES_EXPLOSION), SurvivesExplosionCondition::new, SurvivesExplosionCondition::new);
        registry.registerCondition(TableBonusCondition.class, getKey(BuiltInRegistries.LOOT_CONDITION_TYPE, LootItemConditions.TABLE_BONUS), TableBonusCondition::new, TableBonusCondition::new);
        registry.registerCondition(TimeCheckCondition.class, getKey(BuiltInRegistries.LOOT_CONDITION_TYPE, LootItemConditions.TIME_CHECK), TimeCheckCondition::new, TimeCheckCondition::new);
        registry.registerCondition(ValueCheckCondition.class, getKey(BuiltInRegistries.LOOT_CONDITION_TYPE, LootItemConditions.VALUE_CHECK), ValueCheckCondition::new, ValueCheckCondition::new);
        registry.registerCondition(WeatherCheckCondition.class, getKey(BuiltInRegistries.LOOT_CONDITION_TYPE, LootItemConditions.WEATHER_CHECK), WeatherCheckCondition::new, WeatherCheckCondition::new);
    }
    
    @NotNull
    private static <T> ResourceLocation getKey(Registry<T> registry, T key) {
        ResourceLocation location = registry.getKey(key);
        return Objects.requireNonNull(location);
    }
}
