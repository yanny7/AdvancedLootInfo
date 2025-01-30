package com.yanny.advanced_loot_info.plugin;

import com.yanny.advanced_loot_info.api.AliEntrypoint;
import com.yanny.advanced_loot_info.api.IPlugin;
import com.yanny.advanced_loot_info.api.IRegistry;
import com.yanny.advanced_loot_info.plugin.condition.*;
import com.yanny.advanced_loot_info.plugin.function.*;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import net.minecraftforge.common.loot.CanToolPerformAction;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@AliEntrypoint
public class VanillaPlugin implements IPlugin {
    public static final ResourceLocation UNKNOWN = new ResourceLocation("unknown");

    @Override
    public void register(IRegistry registry) {
        registry.registerFunction(ApplyBonusFunction.class, getKey(LootItemFunctions.APPLY_BONUS), ApplyBonusFunction::new, ApplyBonusFunction::new);
        registry.registerFunction(CopyNameFunction.class, getKey(LootItemFunctions.COPY_NAME), CopyNameFunction::new, CopyNameFunction::new);
        registry.registerFunction(CopyNbtFunction.class, getKey(LootItemFunctions.COPY_NBT), CopyNbtFunction::new, CopyNbtFunction::new);
        registry.registerFunction(CopyStateFunction.class, getKey(LootItemFunctions.COPY_STATE), CopyStateFunction::new, CopyStateFunction::new);
        registry.registerFunction(EnchantRandomlyFunction.class, getKey(LootItemFunctions.ENCHANT_RANDOMLY), EnchantRandomlyFunction::new, EnchantRandomlyFunction::new);
        registry.registerFunction(EnchantWithLevelsFunction.class, getKey(LootItemFunctions.ENCHANT_WITH_LEVELS), EnchantWithLevelsFunction::new, EnchantWithLevelsFunction::new);
        registry.registerFunction(ExplorationMapFunction.class, getKey(LootItemFunctions.EXPLORATION_MAP), ExplorationMapFunction::new, ExplorationMapFunction::new);
        registry.registerFunction(ExplosionDecayFunction.class, getKey(LootItemFunctions.EXPLOSION_DECAY), ExplosionDecayFunction::new, ExplosionDecayFunction::new);
        registry.registerFunction(FillPlayerHeadFunction.class, getKey(LootItemFunctions.FILL_PLAYER_HEAD), FillPlayerHeadFunction::new, FillPlayerHeadFunction::new);
        registry.registerFunction(FurnaceSmeltFunction.class, getKey(LootItemFunctions.FURNACE_SMELT), FurnaceSmeltFunction::new, FurnaceSmeltFunction::new);
        registry.registerFunction(LimitCountFunction.class, getKey(LootItemFunctions.LIMIT_COUNT), LimitCountFunction::new, LimitCountFunction::new);
        registry.registerFunction(LootingEnchantFunction.class, getKey(LootItemFunctions.LOOTING_ENCHANT), LootingEnchantFunction::new, LootingEnchantFunction::new);
        registry.registerFunction(ReferenceFunction.class, getKey(LootItemFunctions.REFERENCE), ReferenceFunction::new, ReferenceFunction::new);
        registry.registerFunction(SetAttributesFunction.class, getKey(LootItemFunctions.SET_ATTRIBUTES), SetAttributesFunction::new, SetAttributesFunction::new);
        registry.registerFunction(SetBannerPatternFunction.class, getKey(LootItemFunctions.SET_BANNER_PATTERN), SetBannerPatternFunction::new, SetBannerPatternFunction::new);
        registry.registerFunction(SetContentsFunction.class, getKey(LootItemFunctions.SET_CONTENTS), SetContentsFunction::new, SetContentsFunction::new);
        registry.registerFunction(SetCountFunction.class, getKey(LootItemFunctions.SET_COUNT), SetCountFunction::new, SetCountFunction::new);
        registry.registerFunction(SetDamageFunction.class, getKey(LootItemFunctions.SET_DAMAGE), SetDamageFunction::new, SetDamageFunction::new);
        registry.registerFunction(SetEnchantmentsFunction.class, getKey(LootItemFunctions.SET_ENCHANTMENTS), SetEnchantmentsFunction::new, SetEnchantmentsFunction::new);
        registry.registerFunction(SetInstrumentFunction.class, getKey(LootItemFunctions.SET_INSTRUMENT), SetInstrumentFunction::new, SetInstrumentFunction::new);
        registry.registerFunction(SetLootTableFunction.class, getKey(LootItemFunctions.SET_LOOT_TABLE), SetLootTableFunction::new, SetLootTableFunction::new);
        registry.registerFunction(SetLoreFunction.class, getKey(LootItemFunctions.SET_LORE), SetLoreFunction::new, SetLoreFunction::new);
        registry.registerFunction(SetNameFunction.class, getKey(LootItemFunctions.SET_NAME), SetNameFunction::new, SetNameFunction::new);
        registry.registerFunction(SetNbtFunction.class, getKey(LootItemFunctions.SET_NBT), SetNbtFunction::new, SetNbtFunction::new);
        registry.registerFunction(SetPotionFunction.class, getKey(LootItemFunctions.SET_POTION), SetPotionFunction::new, SetPotionFunction::new);
        registry.registerFunction(SetStewEffectFunction.class, getKey(LootItemFunctions.SET_STEW_EFFECT), SetStewEffectFunction::new, SetStewEffectFunction::new);
        registry.registerFunction(UnknownFunction.class, UNKNOWN, UnknownFunction::new, UnknownFunction::new);

        registry.registerCondition(AllOfCondition.class, getKey(LootItemConditions.ALL_OF), AllOfCondition::new, AllOfCondition::new);
        registry.registerCondition(AnyOfCondition.class, getKey(LootItemConditions.ANY_OF), AnyOfCondition::new, AnyOfCondition::new);
        registry.registerCondition(BlockStatePropertyCondition.class, getKey(LootItemConditions.BLOCK_STATE_PROPERTY), BlockStatePropertyCondition::new, BlockStatePropertyCondition::new);
        registry.registerCondition(CanToolPerformActionCondition.class, getKey(CanToolPerformAction.LOOT_CONDITION_TYPE), CanToolPerformActionCondition::new, CanToolPerformActionCondition::new);
        registry.registerCondition(DamageSourcePropertiesCondition.class, getKey(LootItemConditions.DAMAGE_SOURCE_PROPERTIES), DamageSourcePropertiesCondition::new, DamageSourcePropertiesCondition::new);
        registry.registerCondition(EntityPropertiesCondition.class, getKey(LootItemConditions.ENTITY_PROPERTIES), EntityPropertiesCondition::new, EntityPropertiesCondition::new);
        registry.registerCondition(EntityScoresCondition.class, getKey(LootItemConditions.ENTITY_SCORES), EntityScoresCondition::new, EntityScoresCondition::new);
        registry.registerCondition(InvertedCondition.class, getKey(LootItemConditions.INVERTED), InvertedCondition::new, InvertedCondition::new);
        registry.registerCondition(KilledByPlayerCondition.class, getKey(LootItemConditions.KILLED_BY_PLAYER), KilledByPlayerCondition::new, KilledByPlayerCondition::new);
        registry.registerCondition(LocationCheckCondition.class, getKey(LootItemConditions.LOCATION_CHECK), LocationCheckCondition::new, LocationCheckCondition::new);
        registry.registerCondition(MatchToolCondition.class, getKey(LootItemConditions.MATCH_TOOL), MatchToolCondition::new, MatchToolCondition::new);
        registry.registerCondition(RandomChanceCondition.class, getKey(LootItemConditions.RANDOM_CHANCE), RandomChanceCondition::new, RandomChanceCondition::new);
        registry.registerCondition(RandomChanceWithLootingCondition.class, getKey(LootItemConditions.RANDOM_CHANCE_WITH_LOOTING), RandomChanceWithLootingCondition::new, RandomChanceWithLootingCondition::new);
        registry.registerCondition(ReferenceCondition.class, getKey(LootItemConditions.REFERENCE), ReferenceCondition::new, ReferenceCondition::new);
        registry.registerCondition(SurvivesExplosionCondition.class, getKey(LootItemConditions.SURVIVES_EXPLOSION), SurvivesExplosionCondition::new, SurvivesExplosionCondition::new);
        registry.registerCondition(TableBonusCondition.class, getKey(LootItemConditions.TABLE_BONUS), TableBonusCondition::new, TableBonusCondition::new);
        registry.registerCondition(TimeCheckCondition.class, getKey(LootItemConditions.TIME_CHECK), TimeCheckCondition::new, TimeCheckCondition::new);
        registry.registerCondition(ValueCheckCondition.class, getKey(LootItemConditions.VALUE_CHECK), ValueCheckCondition::new, ValueCheckCondition::new);
        registry.registerCondition(WeatherCheckCondition.class, getKey(LootItemConditions.WEATHER_CHECK), WeatherCheckCondition::new, WeatherCheckCondition::new);
        registry.registerCondition(UnknownCondition.class, UNKNOWN, UnknownCondition::new, UnknownCondition::new);
    }

    @NotNull
    private static ResourceLocation getKey(LootItemFunctionType key) {
        return getKey(BuiltInRegistries.LOOT_FUNCTION_TYPE, key);
    }

    @NotNull
    private static ResourceLocation getKey(LootItemConditionType key) {
        return getKey(BuiltInRegistries.LOOT_CONDITION_TYPE, key);
    }
    
    @NotNull
    private static <T> ResourceLocation getKey(Registry<T> registry, T key) {
        ResourceLocation location = registry.getKey(key);
        return Objects.requireNonNull(location);
    }
}
