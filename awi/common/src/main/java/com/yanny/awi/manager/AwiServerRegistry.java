package com.yanny.awi.manager;

import com.yanny.aci.manager.ClassKeyedMap;
import com.yanny.aci.manager.CoreServerRegistry;
import com.yanny.aci.manager.ManagedRegistry;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.awi.api.ICommonUtils;
import com.yanny.awi.api.IServerRegistry;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.plugin.server.MissingTooltipUtils;
import com.yanny.awi.plugin.server.PlacementModifierTooltipUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
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
    // tooltips
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, FeatureConfiguration, TooltipBuilder>> featureTooltips = registerClassKeyed("feature tooltips", false, HashMap::new, null);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, PlacementModifier, TooltipBuilder>> placementModifierTooltips = registerClassKeyed("placement modifier tooltips", true, HashMap::new, BuiltInRegistries.PLACEMENT_MODIFIER_TYPE);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, IntProvider, TooltipBuilder>> intProviderTooltips = registerClassKeyed("int provider tooltips", true, HashMap::new, BuiltInRegistries.INT_PROVIDER_TYPE);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, RuleTest, TooltipBuilder>> ruleTestTooltips = registerClassKeyed("rule test tooltips", true, HashMap::new, BuiltInRegistries.RULE_TEST);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, Object, TooltipBuilder>> valueTooltips = registerClassKeyed("value tooltips", true, ClassKeyedMap::new, null);

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
    public @NotNull <T extends FeatureConfiguration> TooltipBuilder getFeatureTooltip(IServerUtils utils, T entry) {
        return featureTooltips.get(entry.getClass())
                .map((e) -> e.apply(utils, entry))
                .orElseGet(() -> MissingTooltipUtils.getMissingFeatureConfigurationTooltip(utils, entry));
    }

    @Override
    public @NotNull <T extends PlacementModifier> TooltipBuilder getPlacementModifierTooltip(IServerUtils utils, T entry) {
        return placementModifierTooltips.get(entry.getClass())
                .map((e) -> e.apply(utils, entry))
                .orElseGet(() -> PlacementModifierTooltipUtils.getMissingPlacementModifierTooltip(utils, entry));
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
