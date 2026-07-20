package com.yanny.awi.plugin.server;

import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.yanny.aci.language.CoreLang;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipContext;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.awi.api.FeatureHolder;
import com.yanny.awi.api.IServerUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Collections;

public class MissingTooltipUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    @NotNull
    public static TooltipBuilder getMissingFeatureConfigurationTooltip(IServerUtils utils, FeatureConfiguration configuration) {
        //TODO auto detected placed feature
        return TooltipBuilder.error("Not implemented");
    }

    @NotNull
    public static FeatureHolder getMissingPlacedFeature(IServerUtils utils, PlacedFeature placedFeature) {
        //TODO auto detected placed feature
        return new FeatureHolder(Collections.emptyList(), TooltipNode.empty());
    }

    @NotNull
    public static TooltipBuilder getMissingPlacementModifierTooltip(IServerUtils utils, PlacementModifier placement) {
        TooltipBuilder tooltip = utils.getValueTooltip(utils, placement.type());

        try {
            RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, utils.lookupProvider());
            //noinspection unchecked
            Codec<PlacementModifier> codec = ((Codec<PlacementModifier>) placement.type().codec());
            JsonElement jsonElement = codec.encodeStart(registryOps, placement).getOrThrow(true, (s) -> {});

            tooltip.add(TooltipUtils.getJsonTooltip(utils, jsonElement));
        } catch (Throwable e) {
//            if (utils.getConfiguration().logMoreStatistics) { FIXME
            LOGGER.warn("Failed to get placement modifier from serialized data for {} in {}", BuiltInRegistries.PLACEMENT_MODIFIER_TYPE.getKey(placement.type()), TooltipContext.get(), e);
//            }

//            TooltipUtils.addObjectFields(utils, tooltip, entry, CompositeEntryBase.class); FIXME
        }

        return tooltip.key(CoreLang.Utils.AUTO_DETECTED);
    }

    @NotNull
    public static TooltipBuilder getMissingIntProviderTooltip(IServerUtils utils, IntProvider provider) {
        TooltipBuilder tooltip = utils.getValueTooltip(utils, provider.getType());

        try {
            RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, utils.lookupProvider());
            //noinspection unchecked
            Codec<IntProvider> codec = ((Codec<IntProvider>) provider.getType().codec());
            JsonElement jsonElement = codec.encodeStart(registryOps, provider).getOrThrow(true, (s) -> {});

            tooltip.add(TooltipUtils.getJsonTooltip(utils, jsonElement));
        } catch (Throwable e) {
//            if (utils.getConfiguration().logMoreStatistics) { FIXME
            LOGGER.warn("Failed to get int provider from serialized data for {} in {}", BuiltInRegistries.INT_PROVIDER_TYPE.getKey(provider.getType()), TooltipContext.get(), e);
//            }

//            TooltipUtils.addObjectFields(utils, tooltip, entry, CompositeEntryBase.class); FIXME
        }

        return tooltip.key(CoreLang.Utils.AUTO_DETECTED);
    }

    @NotNull
    public static TooltipBuilder getMissingRuleTestTooltip(IServerUtils utils, RuleTest test) {
        TooltipBuilder tooltip = utils.getValueTooltip(utils, test.getType());

        try {
            RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, utils.lookupProvider());
            //noinspection unchecked
            Codec<RuleTest> codec = ((Codec<RuleTest>) test.getType().codec());
            JsonElement jsonElement = codec.encodeStart(registryOps, test).getOrThrow(true, (s) -> {});

            tooltip.add(TooltipUtils.getJsonTooltip(utils, jsonElement));
        } catch (Throwable e) {
//            if (utils.getConfiguration().logMoreStatistics) { FIXME
            LOGGER.warn("Failed to get rule test from serialized data for {} in {}", BuiltInRegistries.RULE_TEST.getKey(test.getType()), TooltipContext.get(), e);
//            }

//            TooltipUtils.addObjectFields(utils, tooltip, entry, CompositeEntryBase.class); FIXME
        }

        return tooltip.key(CoreLang.Utils.AUTO_DETECTED);
    }

    @NotNull
    public static TooltipBuilder getMissingHeightProviderTooltip(IServerUtils utils, HeightProvider provider) {
        TooltipBuilder tooltip = utils.getValueTooltip(utils, provider.getType());

        try {
            RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, utils.lookupProvider());
            //noinspection unchecked
            Codec<HeightProvider> codec = ((Codec<HeightProvider>) provider.getType().codec());
            JsonElement jsonElement = codec.encodeStart(registryOps, provider).getOrThrow(true, (s) -> {});

            tooltip.add(TooltipUtils.getJsonTooltip(utils, jsonElement));
        } catch (Throwable e) {
//            if (utils.getConfiguration().logMoreStatistics) { FIXME
            LOGGER.warn("Failed to get height provider from serialized data for {} in {}", BuiltInRegistries.HEIGHT_PROVIDER_TYPE.getKey(provider.getType()), TooltipContext.get(), e);
//            }

//            TooltipUtils.addObjectFields(utils, tooltip, entry, CompositeEntryBase.class); FIXME
        }

        return tooltip.key(CoreLang.Utils.AUTO_DETECTED);
    }

    @NotNull
    public static TooltipBuilder getMissingBlockPredicateTooltip(IServerUtils utils, BlockPredicate predicate) {
        TooltipBuilder tooltip = utils.getValueTooltip(utils, predicate.type());

        try {
            RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, utils.lookupProvider());
            //noinspection unchecked
            Codec<BlockPredicate> codec = ((Codec<BlockPredicate>) predicate.type().codec());
            JsonElement jsonElement = codec.encodeStart(registryOps, predicate).getOrThrow(true, (s) -> {});

            tooltip.add(TooltipUtils.getJsonTooltip(utils, jsonElement));
        } catch (Throwable e) {
//            if (utils.getConfiguration().logMoreStatistics) { FIXME
            LOGGER.warn("Failed to get block predicate from serialized data for {} in {}", BuiltInRegistries.BLOCK_PREDICATE_TYPE.getKey(predicate.type()), TooltipContext.get(), e);
//            }

//            TooltipUtils.addObjectFields(utils, tooltip, entry, CompositeEntryBase.class); FIXME
        }

        return tooltip.key(CoreLang.Utils.AUTO_DETECTED);
    }

    @NotNull
    public static TooltipBuilder getMissingValueTooltip(IServerUtils ignoredUtils, Object value) {
        return TooltipBuilder.error("[" + value.getClass().getTypeName() + "]").key(CoreLang.Utils.NOT_IMPLEMENTED);
    }
}
