package com.yanny.awi.plugin.server;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.JsonOps;
import com.yanny.aci.CommonLogUtils;
import com.yanny.aci.language.CoreLang;
import com.yanny.aci.tooltip.CoreTooltipUtils;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipContext;
import com.yanny.awi.Utils;
import com.yanny.awi.api.IServerUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.FeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.rootplacers.RootPlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import static com.yanny.aci.tooltip.CoreTooltipUtils.getBuiltInRegistryTooltip;

public class MissingTooltipUtils {
    private static final Logger LOGGER = CommonLogUtils.getLogger(Utils.MOD_ID);

    @NotNull
    public static TooltipBuilder getMissingFeatureConfigurationTooltip(IServerUtils utils, FeatureConfiguration configuration) {
//        LOGGER.warn("FeatureConfiguration {} not implemented", configuration.getClass().getSimpleName());
        //TODO auto detected placed feature
        return TooltipBuilder.error("Not implemented");
    }

    @NotNull
    public static TooltipBuilder getMissingPlacementModifierTooltip(IServerUtils utils, PlacementModifier placement) {
        TooltipBuilder tooltip = utils.getValueTooltip(utils, placement.type());

        try {
            RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, utils.lookupProvider());
            //noinspection unchecked
            MapCodec<PlacementModifier> codec = ((MapCodec<PlacementModifier>) placement.type().codec());
            JsonElement jsonElement = codec.codec().encodeStart(registryOps, placement).getOrThrow();

            tooltip.add(TooltipUtils.getJsonTooltip(utils, jsonElement));
        } catch (Throwable e) {
            if (utils.getConfiguration().logMoreStatistics) {
                LOGGER.warn("Failed to get placement modifier from serialized data for {} in {}", BuiltInRegistries.PLACEMENT_MODIFIER_TYPE.getKey(placement.type()), TooltipContext.get(), e);
            }

//            TooltipUtils.addObjectFields(utils, tooltip, entry, CompositeEntryBase.class); FIXME
        }

        return tooltip.key(CoreLang.Utils.AUTO_DETECTED);
    }

    @NotNull
    public static TooltipBuilder getMissingIntProviderTooltip(IServerUtils utils, IntProvider provider) {
        TooltipBuilder tooltip = CoreTooltipUtils.getBuiltInRegistryTooltip(utils, BuiltInRegistries.INT_PROVIDER_TYPE, provider.codec());

        try {
            RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, utils.lookupProvider());
            //noinspection unchecked
            MapCodec<IntProvider> codec = ((MapCodec<IntProvider>) provider.codec());
            JsonElement jsonElement = codec.codec().encodeStart(registryOps, provider).getOrThrow();

            tooltip.add(TooltipUtils.getJsonTooltip(utils, jsonElement));
        } catch (Throwable e) {
            if (utils.getConfiguration().logMoreStatistics) {
                LOGGER.warn("Failed to get int provider from serialized data for {} in {}", BuiltInRegistries.INT_PROVIDER_TYPE.getKey(provider.codec()), TooltipContext.get(), e);
            }

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
            MapCodec<RuleTest> codec = ((MapCodec<RuleTest>) test.getType().codec());
            JsonElement jsonElement = codec.codec().encodeStart(registryOps, test).getOrThrow();

            tooltip.add(TooltipUtils.getJsonTooltip(utils, jsonElement));
        } catch (Throwable e) {
            if (utils.getConfiguration().logMoreStatistics) {
                LOGGER.warn("Failed to get rule test from serialized data for {} in {}", BuiltInRegistries.RULE_TEST.getKey(test.getType()), TooltipContext.get(), e);
            }

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
            MapCodec<HeightProvider> codec = ((MapCodec<HeightProvider>) provider.getType().codec());
            JsonElement jsonElement = codec.codec().encodeStart(registryOps, provider).getOrThrow();

            tooltip.add(TooltipUtils.getJsonTooltip(utils, jsonElement));
        } catch (Throwable e) {
            if (utils.getConfiguration().logMoreStatistics) {
                LOGGER.warn("Failed to get height provider from serialized data for {} in {}", BuiltInRegistries.HEIGHT_PROVIDER_TYPE.getKey(provider.getType()), TooltipContext.get(), e);
            }

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
            MapCodec<BlockPredicate> codec = ((MapCodec<BlockPredicate>) predicate.type().codec());
            JsonElement jsonElement = codec.codec().encodeStart(registryOps, predicate).getOrThrow();

            tooltip.add(TooltipUtils.getJsonTooltip(utils, jsonElement));
        } catch (Throwable e) {
            if (utils.getConfiguration().logMoreStatistics) {
                LOGGER.warn("Failed to get block predicate from serialized data for {} in {}", BuiltInRegistries.BLOCK_PREDICATE_TYPE.getKey(predicate.type()), TooltipContext.get(), e);
            }

//            TooltipUtils.addObjectFields(utils, tooltip, entry, CompositeEntryBase.class); FIXME
        }

        return tooltip.key(CoreLang.Utils.AUTO_DETECTED);
    }

    @NotNull
    public static TooltipBuilder getMissingBlockStateProviderTooltip(IServerUtils utils, BlockStateProvider provider) {
        TooltipBuilder tooltip = utils.getValueTooltip(utils, provider.type());

        try {
            RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, utils.lookupProvider());
            //noinspection unchecked
            MapCodec<BlockStateProvider> codec = ((MapCodec<BlockStateProvider>) provider.type().codec());
            JsonElement jsonElement = codec.codec().encodeStart(registryOps, provider).getOrThrow();

            tooltip.add(TooltipUtils.getJsonTooltip(utils, jsonElement));
        } catch (Throwable e) {
            if (utils.getConfiguration().logMoreStatistics) {
                LOGGER.warn("Failed to get block state provider from serialized data for {} in {}", BuiltInRegistries.BLOCKSTATE_PROVIDER_TYPE.getKey(provider.type()), TooltipContext.get(), e);
            }

//            TooltipUtils.addObjectFields(utils, tooltip, entry, CompositeEntryBase.class); FIXME
        }

        return tooltip.key(CoreLang.Utils.AUTO_DETECTED);
    }

    @NotNull
    public static TooltipBuilder getMissingTreeDecoratorTooltip(IServerUtils utils, TreeDecorator decorator) {
        TooltipBuilder tooltip = utils.getValueTooltip(utils, decorator.type());

        try {
            RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, utils.lookupProvider());
            //noinspection unchecked
            MapCodec<TreeDecorator> codec = ((MapCodec<TreeDecorator>) decorator.type().codec());
            JsonElement jsonElement = codec.codec().encodeStart(registryOps, decorator).getOrThrow();

            tooltip.add(TooltipUtils.getJsonTooltip(utils, jsonElement));
        } catch (Throwable e) {
            if (utils.getConfiguration().logMoreStatistics) {
                LOGGER.warn("Failed to get tree decorator from serialized data for {} in {}", BuiltInRegistries.TREE_DECORATOR_TYPE.getKey(decorator.type()), TooltipContext.get(), e);
            }

//            TooltipUtils.addObjectFields(utils, tooltip, entry, CompositeEntryBase.class); FIXME
        }

        return tooltip.key(CoreLang.Utils.AUTO_DETECTED);
    }

    @NotNull
    public static TooltipBuilder getMissingFeatureSizeTooltip(IServerUtils utils, FeatureSize size) {
        TooltipBuilder tooltip = utils.getValueTooltip(utils, size.type());

        try {
            RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, utils.lookupProvider());
            //noinspection unchecked
            MapCodec<FeatureSize> codec = ((MapCodec<FeatureSize>) size.type().codec());
            JsonElement jsonElement = codec.codec().encodeStart(registryOps, size).getOrThrow();

            tooltip.add(TooltipUtils.getJsonTooltip(utils, jsonElement));
        } catch (Throwable e) {
            if (utils.getConfiguration().logMoreStatistics) {
                LOGGER.warn("Failed to get feature size from serialized data for {} in {}", BuiltInRegistries.FEATURE_SIZE_TYPE.getKey(size.type()), TooltipContext.get(), e);
            }

//            TooltipUtils.addObjectFields(utils, tooltip, entry, CompositeEntryBase.class); FIXME
        }

        return tooltip.key(CoreLang.Utils.AUTO_DETECTED);
    }

    @NotNull
    public static TooltipBuilder getMissingRootPlacerTooltip(IServerUtils utils, RootPlacer placer) {
        TooltipBuilder tooltip = utils.getValueTooltip(utils, placer.type());

        try {
            RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, utils.lookupProvider());
            //noinspection unchecked
            MapCodec<RootPlacer> codec = ((MapCodec<RootPlacer>) placer.type().codec());
            JsonElement jsonElement = codec.codec().encodeStart(registryOps, placer).getOrThrow();

            tooltip.add(TooltipUtils.getJsonTooltip(utils, jsonElement));
        } catch (Throwable e) {
            if (utils.getConfiguration().logMoreStatistics) {
                LOGGER.warn("Failed to get root placer from serialized data for {} in {}", BuiltInRegistries.ROOT_PLACER_TYPE.getKey(placer.type()), TooltipContext.get(), e);
            }

//            TooltipUtils.addObjectFields(utils, tooltip, entry, CompositeEntryBase.class); FIXME
        }

        return tooltip.key(CoreLang.Utils.AUTO_DETECTED);
    }

    @NotNull
    public static TooltipBuilder getMissingFoliagePlacerTooltip(IServerUtils utils, FoliagePlacer placer) {
        TooltipBuilder tooltip = utils.getValueTooltip(utils, placer.type());

        try {
            RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, utils.lookupProvider());
            //noinspection unchecked
            MapCodec<FoliagePlacer> codec = ((MapCodec<FoliagePlacer>) placer.type().codec());
            JsonElement jsonElement = codec.codec().encodeStart(registryOps, placer).getOrThrow();

            tooltip.add(TooltipUtils.getJsonTooltip(utils, jsonElement));
        } catch (Throwable e) {
            if (utils.getConfiguration().logMoreStatistics) {
                LOGGER.warn("Failed to get foliage placer from serialized data for {} in {}", BuiltInRegistries.FOLIAGE_PLACER_TYPE.getKey(placer.type()), TooltipContext.get(), e);
            }

//            TooltipUtils.addObjectFields(utils, tooltip, entry, CompositeEntryBase.class); FIXME
        }

        return tooltip.key(CoreLang.Utils.AUTO_DETECTED);
    }

    @NotNull
    public static TooltipBuilder getMissingTrunkPlacerTooltip(IServerUtils utils, TrunkPlacer placer) {
        TooltipBuilder tooltip = utils.getValueTooltip(utils, placer.type());

        try {
            RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, utils.lookupProvider());
            //noinspection unchecked
            MapCodec<TrunkPlacer> codec = ((MapCodec<TrunkPlacer>) placer.type().codec());
            JsonElement jsonElement = codec.codec().encodeStart(registryOps, placer).getOrThrow();

            tooltip.add(TooltipUtils.getJsonTooltip(utils, jsonElement));
        } catch (Throwable e) {
            if (utils.getConfiguration().logMoreStatistics) {
                LOGGER.warn("Failed to get trunk placer from serialized data for {} in {}", BuiltInRegistries.TRUNK_PLACER_TYPE.getKey(placer.type()), TooltipContext.get(), e);
            }

//            TooltipUtils.addObjectFields(utils, tooltip, entry, CompositeEntryBase.class); FIXME
        }

        return tooltip.key(CoreLang.Utils.AUTO_DETECTED);
    }

    @NotNull
    public static TooltipBuilder getMissingFloatProviderTooltip(IServerUtils utils, FloatProvider provider) {
        TooltipBuilder tooltip = CoreTooltipUtils.getBuiltInRegistryTooltip(utils, BuiltInRegistries.FLOAT_PROVIDER_TYPE, provider.codec());

        try {
            RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, utils.lookupProvider());
            //noinspection unchecked
            MapCodec<FloatProvider> codec = ((MapCodec<FloatProvider>) provider.codec());
            JsonElement jsonElement = codec.codec().encodeStart(registryOps, provider).getOrThrow();

            tooltip.add(TooltipUtils.getJsonTooltip(utils, jsonElement));
        } catch (Throwable e) {
            if (utils.getConfiguration().logMoreStatistics) {
                LOGGER.warn("Failed to get float provider from serialized data for {} in {}", BuiltInRegistries.FLOAT_PROVIDER_TYPE.getKey(provider.codec()), TooltipContext.get(), e);
            }

//            TooltipUtils.addObjectFields(utils, tooltip, entry, CompositeEntryBase.class); FIXME
        }

        return tooltip.key(CoreLang.Utils.AUTO_DETECTED);
    }

    @NotNull
    public static TooltipBuilder getMissingStructureProcessorTooltip(IServerUtils utils, StructureProcessor processor) {
        TooltipBuilder tooltip = getBuiltInRegistryTooltip(utils, BuiltInRegistries.STRUCTURE_PROCESSOR, processor.codec());

        try {
            RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, utils.lookupProvider());
            //noinspection unchecked
            MapCodec<StructureProcessor> codec = ((MapCodec<StructureProcessor>) processor.codec());
            JsonElement jsonElement = codec.codec().encodeStart(registryOps, processor).getOrThrow();

            tooltip.add(TooltipUtils.getJsonTooltip(utils, jsonElement));
        } catch (Throwable e) {
            if (utils.getConfiguration().logMoreStatistics) {
                LOGGER.warn("Failed to get structure processor from serialized data for {} in {}", BuiltInRegistries.STRUCTURE_PROCESSOR.getKey(processor.codec()), TooltipContext.get(), e);
            }

//            TooltipUtils.addObjectFields(utils, tooltip, entry, CompositeEntryBase.class); FIXME
        }

        return tooltip.key(CoreLang.Utils.AUTO_DETECTED);
    }

    @NotNull
    public static TooltipBuilder getMissingValueTooltip(IServerUtils ignoredUtils, Object value) {
        return TooltipBuilder.error("[" + value.getClass().getTypeName() + "]").key(CoreLang.Utils.NOT_IMPLEMENTED);
    }
}
