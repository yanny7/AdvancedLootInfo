package com.yanny.awi.manager;

import com.yanny.aci.api.RangeValue;
import com.yanny.aci.manager.ClassKeyedMap;
import com.yanny.aci.manager.CoreServerRegistry;
import com.yanny.aci.manager.ManagedRegistry;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.awi.api.ICommonUtils;
import com.yanny.awi.api.IServerRegistry;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.plugin.server.MissingTooltipUtils;
import com.yanny.awi.plugin.server.summary.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.Block;
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
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;

public class AwiServerRegistry extends CoreServerRegistry<Object, AwiCommonRegistry, IServerUtils> implements IServerRegistry, IServerUtils, ICommonUtils {
    // collectors
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, FeatureConfiguration, List<Block>>> featureBlockCollector = registerClassKeyed("feature block collectors", false, HashMap::new, null);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, BlockStateProvider, List<Block>>> stateProviderBlockCollector = registerClassKeyed("state provider block collectors", false, HashMap::new, BuiltInRegistries.BLOCKSTATE_PROVIDER_TYPE);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, RootPlacer, List<Block>>> rootPlacerBlockCollector = registerClassKeyed("root placer block collectors", false, HashMap::new, BuiltInRegistries.ROOT_PLACER_TYPE);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, TreeDecorator, List<Block>>> treeDecoratorBlockCollector = registerClassKeyed("tree decorator block collectors", false, HashMap::new, BuiltInRegistries.TREE_DECORATOR_TYPE);
    // tooltips
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, FeatureConfiguration, TooltipBuilder>> featureTooltips = registerClassKeyed("feature tooltips", false, HashMap::new, null);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, PlacementModifier, TooltipBuilder>> placementModifierTooltips = registerClassKeyed("placement modifier tooltips", true, HashMap::new, BuiltInRegistries.PLACEMENT_MODIFIER_TYPE);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, IntProvider, TooltipBuilder>> intProviderTooltips = registerClassKeyed("int provider tooltips", true, HashMap::new, BuiltInRegistries.INT_PROVIDER_TYPE);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, RuleTest, TooltipBuilder>> ruleTestTooltips = registerClassKeyed("rule test tooltips", true, HashMap::new, BuiltInRegistries.RULE_TEST);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, Object, TooltipBuilder>> valueTooltips = registerClassKeyed("value tooltips", true, ClassKeyedMap::new, null);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, HeightProvider, TooltipBuilder>> heightProviderTooltips = registerClassKeyed("height provider tooltips", true, HashMap::new, BuiltInRegistries.HEIGHT_PROVIDER_TYPE);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, BlockPredicate, TooltipBuilder>> blockPredicateTooltips = registerClassKeyed("block predicate tooltips", true, HashMap::new, BuiltInRegistries.BLOCK_PREDICATE_TYPE);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, BlockStateProvider, TooltipBuilder>> blockStateProviderTooltips = registerClassKeyed("block state provider tooltips", true, HashMap::new, BuiltInRegistries.BLOCKSTATE_PROVIDER_TYPE);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, TreeDecorator, TooltipBuilder>> treeDecoratorTooltips = registerClassKeyed("tree decorator tooltips", true, HashMap::new, BuiltInRegistries.TREE_DECORATOR_TYPE);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, FeatureSize, TooltipBuilder>> featureSizeTooltips = registerClassKeyed("feature size tooltips", true, HashMap::new, BuiltInRegistries.FEATURE_SIZE_TYPE);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, RootPlacer, TooltipBuilder>> rootPlacerTooltips = registerClassKeyed("root placer tooltips", true, HashMap::new, BuiltInRegistries.ROOT_PLACER_TYPE);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, FoliagePlacer, TooltipBuilder>> foliagePlacerTooltips = registerClassKeyed("foliage placer tooltips", true, HashMap::new, BuiltInRegistries.FOLIAGE_PLACER_TYPE);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, TrunkPlacer, TooltipBuilder>> trunkPlacerTooltips = registerClassKeyed("trunk placer tooltips", true, HashMap::new, BuiltInRegistries.TRUNK_PLACER_TYPE);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, FloatProvider, TooltipBuilder>> floatProviderTooltips = registerClassKeyed("float provider tooltips", true, HashMap::new, BuiltInRegistries.FLOAT_PROVIDER_TYPE);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, StructureProcessor, TooltipBuilder>> structureProcessorTooltips = registerClassKeyed("structure processor tooltips", true, HashMap::new, BuiltInRegistries.STRUCTURE_PROCESSOR);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, IntProvider, CountSpan>> intSpanPropagators = registerClassKeyed("int span propagators", true, HashMap::new, BuiltInRegistries.INT_PROVIDER_TYPE);
    private final ManagedRegistry<Class<?>, HeightSpanPropagator<HeightProvider>> heightSpanPropagators = registerClassKeyed("height span propagators", true, HashMap::new, BuiltInRegistries.HEIGHT_PROVIDER_TYPE);
    private final ManagedRegistry<Class<?>, PlacementPropagator<PlacementModifier>> placementPropagators = registerClassKeyed("placement propagators", false, HashMap::new, BuiltInRegistries.PLACEMENT_MODIFIER_TYPE);

    public AwiServerRegistry(AwiCommonRegistry registry, ServerLevel level) {
        super(registry, level);
    }

    @Override
    public <T extends FeatureConfiguration> void registerFeatureBlockCollector(Class<T> type, BiFunction<IServerUtils, T, List<Block>> getter) {
        //noinspection unchecked
        featureBlockCollector.put(type, (u, t) -> getter.apply(u, (T) t));
    }

    @Override
    public <T extends BlockStateProvider> void registerStateProviderBlockCollector(Class<T> type, BiFunction<IServerUtils, T, List<Block>> getter) {
        //noinspection unchecked
        stateProviderBlockCollector.put(type, (u, t) -> getter.apply(u, (T) t));
    }

    @Override
    public <T extends RootPlacer> void registerRootPlacerBlockCollector(Class<T> type, BiFunction<IServerUtils, T, List<Block>> getter) {
        //noinspection unchecked
        rootPlacerBlockCollector.put(type, (u, t) -> getter.apply(u, (T) t));
    }

    @Override
    public <T extends TreeDecorator> void registerTreeDecoratorBlockCollector(Class<T> type, BiFunction<IServerUtils, T, List<Block>> getter) {
        //noinspection unchecked
        treeDecoratorBlockCollector.put(type, (u, t) -> getter.apply(u, (T) t));
    }

    @Override
    public <T extends FeatureConfiguration> void registerFeatureTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter) {
        //noinspection unchecked
        featureTooltips.put(type, (u, t) -> getter.apply(u, (T) t));
    }

    @Override
    public <T extends PlacementModifier> void registerPlacementModifierTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter) {
        //noinspection unchecked
        placementModifierTooltips.put(type, (u, t) -> getter.apply(u, (T) t));
    }

    @Override
    public <T extends IntProvider> void registerIntProviderTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter) {
        //noinspection unchecked
        intProviderTooltips.put(type, (u, t) -> getter.apply(u, (T) t));
    }

    @Override
    public <T extends RuleTest> void registerRuleTestTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter) {
        //noinspection unchecked
        ruleTestTooltips.put(type, (u, t) -> getter.apply(u, (T) t));
    }

    @Override
    public <T extends HeightProvider> void registerHeightProviderTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter) {
        //noinspection unchecked
        heightProviderTooltips.put(type, (u, t) -> getter.apply(u, (T) t));
    }

    @Override
    public <T extends BlockPredicate> void registerBlockPredicateTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter) {
        //noinspection unchecked
        blockPredicateTooltips.put(type, (u, t) -> getter.apply(u, (T) t));
    }

    @Override
    public <T extends BlockStateProvider> void registerBlockStateProviderTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter) {
        //noinspection unchecked
        blockStateProviderTooltips.put(type, (u, t) -> getter.apply(u, (T) t));
    }

    @Override
    public <T extends TreeDecorator> void registerTreeDecoratorTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter) {
        //noinspection unchecked
        treeDecoratorTooltips.put(type, (u, t) -> getter.apply(u, (T) t));
    }

    @Override
    public <T extends FeatureSize> void registerFeatureSizeTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter) {
        //noinspection unchecked
        featureSizeTooltips.put(type, (u, t) -> getter.apply(u, (T) t));
    }

    @Override
    public <T extends RootPlacer> void registerRootPlacerTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter) {
        //noinspection unchecked
        rootPlacerTooltips.put(type, (u, t) -> getter.apply(u, (T) t));
    }

    @Override
    public <T extends FoliagePlacer> void registerFoliagePlacerTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter) {
        //noinspection unchecked
        foliagePlacerTooltips.put(type, (u, t) -> getter.apply(u, (T) t));
    }

    @Override
    public <T extends TrunkPlacer> void registerTrunkPlacerTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter) {
        //noinspection unchecked
        trunkPlacerTooltips.put(type, (u, t) -> getter.apply(u, (T) t));
    }

    @Override
    public <T extends FloatProvider> void registerFloatProviderTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter) {
        //noinspection unchecked
        floatProviderTooltips.put(type, (u, t) -> getter.apply(u, (T) t));
    }

    @Override
    public <T extends StructureProcessor> void registerStructureProcessorTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter) {
        //noinspection unchecked
        structureProcessorTooltips.put(type, (u, t) -> getter.apply(u, (T) t));
    }

    @Override
    public <T extends IntProvider> void registerIntSpanPropagator(Class<T> type, BiFunction<IServerUtils, T, CountSpan> getter) {
        //noinspection unchecked
        intSpanPropagators.put(type, (u, p) -> getter.apply(u, (T) p));
    }

    @Override
    public <T extends HeightProvider> void registerHeightSpanPropagator(Class<T> type, HeightSpanPropagator<T> getter) {
        //noinspection unchecked
        heightSpanPropagators.put(type, (u, p, c) -> getter.apply(u, (T) p, c));
    }

    @Override
    public <T extends PlacementModifier> void registerPlacementPropagator(Class<T> type, PlacementPropagator<T> getter) {
        //noinspection unchecked
        placementPropagators.put(type, (u, m, c) -> getter.apply(u, (T) m, c));
    }

    @Override
    public <T> void registerValueTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter) {
        valueTooltips.put(type, (u, v) -> getter.apply(u, type.cast(v)));
    }

    @NotNull
    @Override
    public <T extends FeatureConfiguration> List<Block> collectBlocks(IServerUtils utils, T entry) {
        return featureBlockCollector.get(entry.getClass())
                .map((e) -> e.apply(utils, entry))
                .orElseGet(List::of); //TODO log missing collector ?
    }

    @NotNull
    @Override
    public <T extends BlockStateProvider> List<Block> collectBlocks(IServerUtils utils, T entry) {
        return stateProviderBlockCollector.get(entry.getClass())
                .map((e) -> e.apply(utils, entry))
                .orElseGet(List::of); //TODO log missing collector ?
    }

    @Override
    public @NotNull <T extends RootPlacer> List<Block> collectBlocks(IServerUtils utils, T entry) {
        return rootPlacerBlockCollector.get(entry.getClass())
                .map((e) -> e.apply(utils, entry))
                .orElseGet(List::of); //TODO log missing collector ?
    }

    @Override
    public @NotNull <T extends TreeDecorator> List<Block> collectBlocks(IServerUtils utils, T entry) {
        return treeDecoratorBlockCollector.get(entry.getClass())
                .map((e) -> e.apply(utils, entry))
                .orElseGet(List::of); //TODO log missing collector ?
    }

    @Override
    public @NotNull <T extends FeatureConfiguration> TooltipBuilder getFeatureTooltip(IServerUtils utils, T entry) {
        return featureTooltips.get(entry.getClass())
                .map((e) -> e.apply(utils, entry))
                .orElseGet(() -> MissingTooltipUtils.getMissingFeatureConfigurationTooltip(utils, entry));
    }

    @Override
    public @NotNull <T extends PlacementModifier> TooltipBuilder getPlacementModifierTooltip(IServerUtils utils, T entry) {
        return placementModifierTooltips.get(entry.getClass())
                .map((e) -> e.apply(utils, entry))
                .orElseGet(() -> MissingTooltipUtils.getMissingPlacementModifierTooltip(utils, entry));
    }

    @NotNull
    @Override
    public <T extends IntProvider> TooltipBuilder getIntProviderTooltip(IServerUtils utils, T entry) {
        return intProviderTooltips.get(entry.getClass())
                .map((e) -> e.apply(utils, entry))
                .orElseGet(() -> MissingTooltipUtils.getMissingIntProviderTooltip(utils, entry));
    }

    @NotNull
    @Override
    public <T extends RuleTest> TooltipBuilder getRuleTestTooltip(IServerUtils utils, T entry) {
        return ruleTestTooltips.get(entry.getClass())
                .map((e) -> e.apply(utils, entry))
                .orElseGet(() -> MissingTooltipUtils.getMissingRuleTestTooltip(utils, entry));
    }

    @Override
    public @NotNull <T extends HeightProvider> TooltipBuilder getHeightProviderTooltip(IServerUtils utils, T entry) {
        return heightProviderTooltips.get(entry.getClass())
                .map((e) -> e.apply(utils, entry))
                .orElseGet(() -> MissingTooltipUtils.getMissingHeightProviderTooltip(utils, entry));
    }

    @Override
    public @NotNull <T extends BlockPredicate> TooltipBuilder getBlockPredicateTooltip(IServerUtils utils, T entry) {
        return blockPredicateTooltips.get(entry.getClass())
                .map((e) -> e.apply(utils, entry))
                .orElseGet(() -> MissingTooltipUtils.getMissingBlockPredicateTooltip(utils, entry));
    }

    @NotNull
    @Override
    public <T extends BlockStateProvider> TooltipBuilder getBlockStateProviderTooltip(IServerUtils utils, T entry) {
        return blockStateProviderTooltips.get(entry.getClass())
                .map((e) -> e.apply(utils, entry))
                .orElseGet(() -> MissingTooltipUtils.getMissingBlockStateProviderTooltip(utils, entry));
    }

    @NotNull
    @Override
    public <T extends TreeDecorator> TooltipBuilder getTreeDecoratorTooltip(IServerUtils utils, T entry) {
        return treeDecoratorTooltips.get(entry.getClass())
                .map((e) -> e.apply(utils, entry))
                .orElseGet(() -> MissingTooltipUtils.getMissingTreeDecoratorTooltip(utils, entry));
    }

    @NotNull
    @Override
    public <T extends FeatureSize> TooltipBuilder getFeatureSizeTooltip(IServerUtils utils, T entry) {
        return featureSizeTooltips.get(entry.getClass())
                .map((e) -> e.apply(utils, entry))
                .orElseGet(() -> MissingTooltipUtils.getMissingFeatureSizeTooltip(utils, entry));
    }

    @NotNull
    @Override
    public <T extends RootPlacer> TooltipBuilder getRootPlacerTooltip(IServerUtils utils, T entry) {
        return rootPlacerTooltips.get(entry.getClass())
                .map((e) -> e.apply(utils, entry))
                .orElseGet(() -> MissingTooltipUtils.getMissingRootPlacerTooltip(utils, entry));
    }

    @NotNull
    @Override
    public <T extends FoliagePlacer> TooltipBuilder getFoliagePlacerTooltip(IServerUtils utils, T entry) {
        return foliagePlacerTooltips.get(entry.getClass())
                .map((e) -> e.apply(utils, entry))
                .orElseGet(() -> MissingTooltipUtils.getMissingFoliagePlacerTooltip(utils, entry));
    }

    @NotNull
    @Override
    public <T extends TrunkPlacer> TooltipBuilder getTrunkPlacerTooltip(IServerUtils utils, T entry) {
        return trunkPlacerTooltips.get(entry.getClass())
                .map((e) -> e.apply(utils, entry))
                .orElseGet(() -> MissingTooltipUtils.getMissingTrunkPlacerTooltip(utils, entry));
    }

    @NotNull
    @Override
    public <T extends FloatProvider> TooltipBuilder getFloatProviderTooltip(IServerUtils utils, T entry) {
        return floatProviderTooltips.get(entry.getClass())
                .map((e) -> e.apply(utils, entry))
                .orElseGet(() -> MissingTooltipUtils.getMissingFloatProviderTooltip(utils, entry));
    }

    @NotNull
    @Override
    public <T extends StructureProcessor> TooltipBuilder getStructureProcessorTooltip(IServerUtils utils, T entry) {
        return structureProcessorTooltips.get(entry.getClass())
                .map((e) -> e.apply(utils, entry))
                .orElseGet(() -> MissingTooltipUtils.getMissingStructureProcessorTooltip(utils, entry));
    }

    @NotNull
    @Override
    public <T extends IntProvider> CountSpan getIntSpan(IServerUtils utils, T provider) {
        return intSpanPropagators.get(provider.getClass())
                .map((e) -> e.apply(utils, provider))
                // fallback: even an unregistered provider exposes a generic min/max range
                .orElseGet(() -> CountSpan.unknown(new RangeValue(provider.minInclusive(), provider.maxInclusive())));
    }

    @NotNull
    @Override
    public <T extends HeightProvider> HeightSpan getHeightSpan(IServerUtils utils, T provider, ColumnContext ctx) {
        return heightSpanPropagators.get(provider.getClass())
                .map((e) -> e.apply(utils, provider, ctx))
                .orElseGet(HeightSpan::unknown);
    }

    @NotNull
    @Override
    public <T extends PlacementModifier> PlacementContribution getPlacementContribution(IServerUtils utils, T modifier, ColumnContext ctx) {
        return placementPropagators.get(modifier.getClass())
                .map((e) -> e.apply(utils, modifier, ctx))
                .orElse(PlacementContribution.EMPTY);
    }

    @NotNull
    @Override
    public <T> TooltipBuilder getValueTooltip(IServerUtils utils, @Nullable T value) {
        if (value == null) {
            return TooltipBuilder.empty();
        }

        Class<?> valueClass = value.getClass();

        if (valueClass.isArray()) {
            return TooltipBuilder.branch((b) -> {
                for (int i = 0; i < Array.getLength(value); i++) {
                    b.add(utils.getValueTooltip(utils, Array.get(value, i)));
                }
            });
        } else {
            return valueTooltips.get(valueClass)
                    .map((v) -> v.apply(utils, value))
                    .orElseGet(() -> MissingTooltipUtils.getMissingValueTooltip(utils, value));
        }
    }

    @Override
    public void printRuntimeInfo() {
        super.printRuntimeInfo();
    }
}
