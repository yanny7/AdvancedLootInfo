package com.yanny.awi.plugin.server;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.yanny.aci.CommonLogUtils;
import com.yanny.aci.language.CoreLang;
import com.yanny.aci.manager.ManagedRegistry;
import com.yanny.aci.tooltip.CoreTooltipUtils;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipContext;
import com.yanny.awi.Utils;
import com.yanny.awi.api.IServerUtils;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
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

public class MissingTooltipUtils {
    private static final Logger LOGGER = CommonLogUtils.getLogger(Utils.MOD_ID);

    @NotNull
    public static TooltipBuilder getMissingFeatureConfigurationTooltip(IServerUtils utils, FeatureConfiguration configuration) {
//        LOGGER.warn("FeatureConfiguration {} not implemented", configuration.getClass().getSimpleName());
        //TODO auto detected placed feature
        return TooltipBuilder.error("Not implemented");
    }

    @NotNull
    public static AutoDetected getMissingPlacementModifierTooltip(IServerUtils utils, PlacementModifier placement) {
        return serialized(utils, BuiltInRegistries.PLACEMENT_MODIFIER_TYPE, placement.type(), placement.type().codec(), placement, "placement modifier");
    }

    @NotNull
    public static AutoDetected getMissingRuleTestTooltip(IServerUtils utils, RuleTest test) {
        return serialized(utils, BuiltInRegistries.RULE_TEST, test.getType(), test.getType().codec(), test, "rule test");
    }

    @NotNull
    public static AutoDetected getMissingHeightProviderTooltip(IServerUtils utils, HeightProvider provider) {
        return serialized(utils, BuiltInRegistries.HEIGHT_PROVIDER_TYPE, provider.getType(), provider.getType().codec(), provider, "height provider");
    }

    @NotNull
    public static AutoDetected getMissingBlockPredicateTooltip(IServerUtils utils, BlockPredicate predicate) {
        return serialized(utils, BuiltInRegistries.BLOCK_PREDICATE_TYPE, predicate.type(), predicate.type().codec(), predicate, "block predicate");
    }

    @NotNull
    public static AutoDetected getMissingBlockStateProviderTooltip(IServerUtils utils, BlockStateProvider provider) {
        return serialized(utils, BuiltInRegistries.BLOCKSTATE_PROVIDER_TYPE, provider.type(), provider.type().codec(), provider, "block state provider");
    }

    @NotNull
    public static AutoDetected getMissingTreeDecoratorTooltip(IServerUtils utils, TreeDecorator decorator) {
        return serialized(utils, BuiltInRegistries.TREE_DECORATOR_TYPE, decorator.type(), decorator.type().codec(), decorator, "tree decorator");
    }

    @NotNull
    public static AutoDetected getMissingFeatureSizeTooltip(IServerUtils utils, FeatureSize size) {
        return serialized(utils, BuiltInRegistries.FEATURE_SIZE_TYPE, size.type(), size.type().codec(), size, "feature size");
    }

    @NotNull
    public static AutoDetected getMissingRootPlacerTooltip(IServerUtils utils, RootPlacer placer) {
        return serialized(utils, BuiltInRegistries.ROOT_PLACER_TYPE, placer.type(), placer.type().codec(), placer, "root placer");
    }

    @NotNull
    public static AutoDetected getMissingFoliagePlacerTooltip(IServerUtils utils, FoliagePlacer placer) {
        return serialized(utils, BuiltInRegistries.FOLIAGE_PLACER_TYPE, placer.type(), placer.type().codec(), placer, "foliage placer");
    }

    @NotNull
    public static AutoDetected getMissingTrunkPlacerTooltip(IServerUtils utils, TrunkPlacer placer) {
        return serialized(utils, BuiltInRegistries.TRUNK_PLACER_TYPE, placer.type(), placer.type().codec(), placer, "trunk placer");
    }

    @NotNull
    public static AutoDetected getMissingStructureProcessorTooltip(IServerUtils utils, StructureProcessor processor) {
        return serialized(utils, BuiltInRegistries.STRUCTURE_PROCESSOR, processor.getType(), processor.getType().codec(), processor, "structure processor");
    }

    @NotNull
    public static TooltipBuilder getMissingValueTooltip(IServerUtils ignoredUtils, Object value) {
        return TooltipBuilder.error("[" + ManagedRegistry.shortClassName(value.getClass()) + "]")
                .add(CoreTooltipUtils.getClassTooltip(value.getClass()))
                .key(CoreLang.Utils.NOT_IMPLEMENTED);
    }

    @NotNull
    private static <T> AutoDetected serialized(IServerUtils utils, Registry<T> registry, T type, MapCodec<?> codec, Object value, String label) {
        TooltipBuilder tooltip = utils.getValueTooltip(utils, type).add(CoreTooltipUtils.getClassTooltip(value.getClass()));
        boolean serialized;

        try {
            RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, utils.lookupProvider());
            //noinspection unchecked
            JsonElement jsonElement = ((MapCodec<Object>) codec).codec().encodeStart(registryOps, value).getOrThrow();

            tooltip.add(TooltipUtils.getJsonTooltip(utils, jsonElement));
            serialized = true;
        } catch (Throwable e) {
            if (utils.getConfiguration().logMoreStatistics) {
                LOGGER.warn("Failed to get {} from serialized data for {} in {}", label, registry.getKey(type), TooltipContext.get(), e);
            }

            serialized = false;
        }

        return new AutoDetected(tooltip.key(CoreLang.Utils.AUTO_DETECTED), serialized);
    }

    public record AutoDetected(TooltipBuilder tooltip, boolean serialized) {
    }
}
