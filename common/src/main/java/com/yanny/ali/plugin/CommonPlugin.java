package com.yanny.ali.plugin;

import com.yanny.ali.api.*;
import com.yanny.ali.mixin.MixinBinomialDistributionGenerator;
import com.yanny.ali.mixin.MixinUniformGenerator;
import com.yanny.ali.plugin.condition.*;
import com.yanny.ali.plugin.entry.*;
import com.yanny.ali.plugin.function.*;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import net.minecraft.world.level.storage.loot.providers.number.LootNumberProviderType;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class CommonPlugin implements IPlugin {
    public static final ResourceLocation UNKNOWN = new ResourceLocation("unknown");

    @Override
    public void registerCommon(ICommonRegistry registry) {
        registry.registerFunction(ApplyBonusAliFunction.class, getKey(LootItemFunctions.APPLY_BONUS), ApplyBonusAliFunction::new, ApplyBonusAliFunction::new);
        registry.registerFunction(CopyNameAliFunction.class, getKey(LootItemFunctions.COPY_NAME), CopyNameAliFunction::new, CopyNameAliFunction::new);
        registry.registerFunction(CopyNbtAliFunction.class, getKey(LootItemFunctions.COPY_NBT), CopyNbtAliFunction::new, CopyNbtAliFunction::new);
        registry.registerFunction(CopyStateAliFunction.class, getKey(LootItemFunctions.COPY_STATE), CopyStateAliFunction::new, CopyStateAliFunction::new);
        registry.registerFunction(EnchantRandomlyAliFunction.class, getKey(LootItemFunctions.ENCHANT_RANDOMLY), EnchantRandomlyAliFunction::new, EnchantRandomlyAliFunction::new);
        registry.registerFunction(EnchantWithLevelsAliFunction.class, getKey(LootItemFunctions.ENCHANT_WITH_LEVELS), EnchantWithLevelsAliFunction::new, EnchantWithLevelsAliFunction::new);
        registry.registerFunction(ExplorationMapAliFunction.class, getKey(LootItemFunctions.EXPLORATION_MAP), ExplorationMapAliFunction::new, ExplorationMapAliFunction::new);
        registry.registerFunction(ExplosionDecayAliFunction.class, getKey(LootItemFunctions.EXPLOSION_DECAY), ExplosionDecayAliFunction::new, ExplosionDecayAliFunction::new);
        registry.registerFunction(FillPlayerHeadAliFunction.class, getKey(LootItemFunctions.FILL_PLAYER_HEAD), FillPlayerHeadAliFunction::new, FillPlayerHeadAliFunction::new);
        registry.registerFunction(FurnaceSmeltAliFunction.class, getKey(LootItemFunctions.FURNACE_SMELT), FurnaceSmeltAliFunction::new, FurnaceSmeltAliFunction::new);
        registry.registerFunction(LimitCountAliFunction.class, getKey(LootItemFunctions.LIMIT_COUNT), LimitCountAliFunction::new, LimitCountAliFunction::new);
        registry.registerFunction(LootingEnchantAliFunction.class, getKey(LootItemFunctions.LOOTING_ENCHANT), LootingEnchantAliFunction::new, LootingEnchantAliFunction::new);
        registry.registerFunction(ReferenceAliFunction.class, getKey(LootItemFunctions.REFERENCE), ReferenceAliFunction::new, ReferenceAliFunction::new);
        registry.registerFunction(SequenceAliFunction.class, getKey(LootItemFunctions.SEQUENCE), SequenceAliFunction::new, SequenceAliFunction::new);
        registry.registerFunction(SetAttributesAliFunction.class, getKey(LootItemFunctions.SET_ATTRIBUTES), SetAttributesAliFunction::new, SetAttributesAliFunction::new);
        registry.registerFunction(SetBannerPatternAliFunction.class, getKey(LootItemFunctions.SET_BANNER_PATTERN), SetBannerPatternAliFunction::new, SetBannerPatternAliFunction::new);
        registry.registerFunction(SetContentsAliFunction.class, getKey(LootItemFunctions.SET_CONTENTS), SetContentsAliFunction::new, SetContentsAliFunction::new);
        registry.registerFunction(SetCountAliFunction.class, getKey(LootItemFunctions.SET_COUNT), SetCountAliFunction::new, SetCountAliFunction::new);
        registry.registerFunction(SetDamageAliFunction.class, getKey(LootItemFunctions.SET_DAMAGE), SetDamageAliFunction::new, SetDamageAliFunction::new);
        registry.registerFunction(SetEnchantmentsAliFunction.class, getKey(LootItemFunctions.SET_ENCHANTMENTS), SetEnchantmentsAliFunction::new, SetEnchantmentsAliFunction::new);
        registry.registerFunction(SetInstrumentAliFunction.class, getKey(LootItemFunctions.SET_INSTRUMENT), SetInstrumentAliFunction::new, SetInstrumentAliFunction::new);
        registry.registerFunction(SetLootTableAliFunction.class, getKey(LootItemFunctions.SET_LOOT_TABLE), SetLootTableAliFunction::new, SetLootTableAliFunction::new);
        registry.registerFunction(SetLoreAliFunction.class, getKey(LootItemFunctions.SET_LORE), SetLoreAliFunction::new, SetLoreAliFunction::new);
        registry.registerFunction(SetNameAliFunction.class, getKey(LootItemFunctions.SET_NAME), SetNameAliFunction::new, SetNameAliFunction::new);
        registry.registerFunction(SetNbtAliFunction.class, getKey(LootItemFunctions.SET_NBT), SetNbtAliFunction::new, SetNbtAliFunction::new);
        registry.registerFunction(SetPotionAliFunction.class, getKey(LootItemFunctions.SET_POTION), SetPotionAliFunction::new, SetPotionAliFunction::new);
        registry.registerFunction(SetStewEffectAliFunction.class, getKey(LootItemFunctions.SET_STEW_EFFECT), SetStewEffectAliFunction::new, SetStewEffectAliFunction::new);
        registry.registerFunction(UnknownAliFunction.class, UNKNOWN, UnknownAliFunction::new, UnknownAliFunction::new);

        registry.registerCondition(AllOfAliCondition.class, getKey(LootItemConditions.ALL_OF), AllOfAliCondition::new, AllOfAliCondition::new);
        registry.registerCondition(AnyOfAliCondition.class, getKey(LootItemConditions.ANY_OF), AnyOfAliCondition::new, AnyOfAliCondition::new);
        registry.registerCondition(BlockStatePropertyAliCondition.class, getKey(LootItemConditions.BLOCK_STATE_PROPERTY), BlockStatePropertyAliCondition::new, BlockStatePropertyAliCondition::new);
        registry.registerCondition(DamageSourcePropertiesAliCondition.class, getKey(LootItemConditions.DAMAGE_SOURCE_PROPERTIES), DamageSourcePropertiesAliCondition::new, DamageSourcePropertiesAliCondition::new);
        registry.registerCondition(EntityPropertiesAliCondition.class, getKey(LootItemConditions.ENTITY_PROPERTIES), EntityPropertiesAliCondition::new, EntityPropertiesAliCondition::new);
        registry.registerCondition(EntityScoresAliCondition.class, getKey(LootItemConditions.ENTITY_SCORES), EntityScoresAliCondition::new, EntityScoresAliCondition::new);
        registry.registerCondition(InvertedAliCondition.class, getKey(LootItemConditions.INVERTED), InvertedAliCondition::new, InvertedAliCondition::new);
        registry.registerCondition(KilledByPlayerAliCondition.class, getKey(LootItemConditions.KILLED_BY_PLAYER), KilledByPlayerAliCondition::new, KilledByPlayerAliCondition::new);
        registry.registerCondition(LocationCheckAliCondition.class, getKey(LootItemConditions.LOCATION_CHECK), LocationCheckAliCondition::new, LocationCheckAliCondition::new);
        registry.registerCondition(MatchToolCondition.class, getKey(LootItemConditions.MATCH_TOOL), MatchToolCondition::new, MatchToolCondition::new);
        registry.registerCondition(RandomChanceAliCondition.class, getKey(LootItemConditions.RANDOM_CHANCE), RandomChanceAliCondition::new, RandomChanceAliCondition::new);
        registry.registerCondition(RandomChanceWithLootingAliCondition.class, getKey(LootItemConditions.RANDOM_CHANCE_WITH_LOOTING), RandomChanceWithLootingAliCondition::new, RandomChanceWithLootingAliCondition::new);
        registry.registerCondition(ReferenceAliCondition.class, getKey(LootItemConditions.REFERENCE), ReferenceAliCondition::new, ReferenceAliCondition::new);
        registry.registerCondition(SurvivesExplosionAliCondition.class, getKey(LootItemConditions.SURVIVES_EXPLOSION), SurvivesExplosionAliCondition::new, SurvivesExplosionAliCondition::new);
        registry.registerCondition(TableBonusAliCondition.class, getKey(LootItemConditions.TABLE_BONUS), TableBonusAliCondition::new, TableBonusAliCondition::new);
        registry.registerCondition(TimeCheckAliCondition.class, getKey(LootItemConditions.TIME_CHECK), TimeCheckAliCondition::new, TimeCheckAliCondition::new);
        registry.registerCondition(ValueCheckAliCondition.class, getKey(LootItemConditions.VALUE_CHECK), ValueCheckAliCondition::new, ValueCheckAliCondition::new);
        registry.registerCondition(WeatherCheckAliCondition.class, getKey(LootItemConditions.WEATHER_CHECK), WeatherCheckAliCondition::new, WeatherCheckAliCondition::new);
        registry.registerCondition(UnknownAliCondition.class, UNKNOWN, UnknownAliCondition::new, UnknownAliCondition::new);

        registry.registerEntry(EmptyEntry.class, getKey(LootPoolEntries.EMPTY), EmptyEntry::new, EmptyEntry::new);
        registry.registerEntry(ItemEntry.class, getKey(LootPoolEntries.ITEM), ItemEntry::new, ItemEntry::new);
        registry.registerEntry(ReferenceEntry.class, getKey(LootPoolEntries.REFERENCE), ReferenceEntry::new, ReferenceEntry::new);
        registry.registerEntry(DynamicEntry.class, getKey(LootPoolEntries.DYNAMIC), DynamicEntry::new, DynamicEntry::new);
        registry.registerEntry(TagEntry.class, getKey(LootPoolEntries.TAG), TagEntry::new, TagEntry::new);
        registry.registerEntry(AlternativesEntry.class, getKey(LootPoolEntries.ALTERNATIVES), AlternativesEntry::new, AlternativesEntry::new);
        registry.registerEntry(SequentialEntry.class, getKey(LootPoolEntries.SEQUENCE), SequentialEntry::new, SequentialEntry::new);
        registry.registerEntry(GroupEntry.class, getKey(LootPoolEntries.GROUP), GroupEntry::new, GroupEntry::new);
        registry.registerEntry(UnknownEntry.class, UNKNOWN, UnknownEntry::new, UnknownEntry::new);

        registry.registerNumberProvider(getKey(NumberProviders.CONSTANT), CommonPlugin::convertConstant);
        registry.registerNumberProvider(getKey(NumberProviders.UNIFORM), CommonPlugin::convertUniform);
        registry.registerNumberProvider(getKey(NumberProviders.BINOMIAL), CommonPlugin::convertBinomial);
        registry.registerNumberProvider(getKey(NumberProviders.SCORE), CommonPlugin::convertScore);
        registry.registerNumberProvider(UNKNOWN, CommonPlugin::convertUnknown);
    }

    @Override
    public void registerClient(IClientRegistry registry) {
        ClientPlugin.initialize(registry);
    }

    @NotNull
    static RangeValue convertConstant(IContext context, NumberProvider numberProvider) {
        LootContext lootContext = context.lootContext();

        if (lootContext != null) {
            return new RangeValue(numberProvider.getFloat(lootContext));
        } else {
            throw new IllegalStateException("LootContext is null!");
        }
    }

    @NotNull
    static RangeValue convertUniform(IContext context, NumberProvider numberProvider) {
        MixinUniformGenerator uniformGenerator = (MixinUniformGenerator) numberProvider;
        return new RangeValue(context.utils().convertNumber(context, uniformGenerator.getMin()).min(),
                context.utils().convertNumber(context, uniformGenerator.getMax()).max());
    }

    @NotNull
    static RangeValue convertBinomial(IContext context, NumberProvider numberProvider) {
        MixinBinomialDistributionGenerator binomialGenerator = (MixinBinomialDistributionGenerator) numberProvider;
        LootContext lootContext = context.lootContext();

        if (lootContext != null) {
            return new RangeValue(0, binomialGenerator.getN().getFloat(lootContext));
        } else {
            throw new IllegalStateException("LootContext is null!");
        }
    }

    @NotNull
    static RangeValue convertScore(IContext context, NumberProvider numberProvider) {
        return new RangeValue(true, false);
    }

    @NotNull
    static RangeValue convertUnknown(IContext context, NumberProvider numberProvider) {
        return new RangeValue(false, true);
    }

    @NotNull
    static ResourceLocation getKey(LootItemFunctionType key) {
        return getKey(BuiltInRegistries.LOOT_FUNCTION_TYPE, key);
    }

    @NotNull
    static ResourceLocation getKey(LootItemConditionType key) {
        return getKey(BuiltInRegistries.LOOT_CONDITION_TYPE, key);
    }

    @NotNull
    static ResourceLocation getKey(LootPoolEntryType key) {
        return getKey(BuiltInRegistries.LOOT_POOL_ENTRY_TYPE, key);
    }

    @NotNull
    static ResourceLocation getKey(LootNumberProviderType key) {
        return getKey(BuiltInRegistries.LOOT_NUMBER_PROVIDER_TYPE, key);
    }

    @NotNull
    static <T> ResourceLocation getKey(Registry<T> registry, T key) {
        ResourceLocation location = registry.getKey(key);
        return Objects.requireNonNull(location);
    }
}
