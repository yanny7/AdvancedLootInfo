package com.yanny.awi.manager;

import com.yanny.aci.manager.ClassKeyedMap;
import com.yanny.aci.manager.CoreServerRegistry;
import com.yanny.aci.manager.ManagedRegistry;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.awi.api.ICommonUtils;
import com.yanny.awi.api.IServerRegistry;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.plugin.server.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;

public class AwiServerRegistry extends CoreServerRegistry<Object, AwiCommonRegistry, IServerUtils> implements IServerRegistry, IServerUtils, ICommonUtils {
    // collectors
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, FeatureConfiguration, List<Item>>> itemCollectors = registerClassKeyed("item collectors", false, HashMap::new, null);
    // tooltips
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, FeatureConfiguration, TooltipNode>> featureTooltips = registerClassKeyed("feature tooltips", false, HashMap::new, null);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, PlacementModifier, TooltipNode>> placementModifierTooltips = registerClassKeyed("placement modifier tooltips", true, HashMap::new, BuiltInRegistries.PLACEMENT_MODIFIER_TYPE);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, IntProvider, TooltipBuilder>> intProviderTooltips = registerClassKeyed("int provider tooltips", true, HashMap::new, BuiltInRegistries.INT_PROVIDER_TYPE);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, RuleTest, TooltipBuilder>> ruleTestTooltips = registerClassKeyed("rule test tooltips", true, HashMap::new, BuiltInRegistries.RULE_TEST);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, Object, TooltipBuilder>> valueTooltips = registerClassKeyed("value tooltips", true, ClassKeyedMap::new, null);

    public AwiServerRegistry(AwiCommonRegistry registry, ServerLevel level) {
        super(registry, level);
    }

    @Override
    public <T extends FeatureConfiguration> void registerItemCollector(Class<T> type, BiFunction<IServerUtils, T, List<Item>> getter) {
        //noinspection unchecked
        itemCollectors.put(type, (u, t) -> getter.apply(u, (T) t));
    }

    @Override
    public <T extends FeatureConfiguration> void registerFeatureTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipNode> getter) {
        //noinspection unchecked
        featureTooltips.put(type, (u, t) -> getter.apply(u, (T) t));
    }

    @Override
    public <T extends PlacementModifier> void registerPlacementModifierTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipNode> getter) {
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
    public <T extends FeatureConfiguration> List<Item> getItemCollector(IServerUtils utils, T entry) {
        return itemCollectors.get(entry.getClass())
                .map((e) -> e.apply(utils, entry))
                .orElseGet(List::of);
    }

    @NotNull
    @Override
    public <T extends FeatureConfiguration> TooltipNode getFeatureTooltip(IServerUtils utils, T entry) {
        return featureTooltips.get(entry.getClass())
                .map((e) -> e.apply(utils, entry))
                .orElseGet(() -> FeatureConfigurationTooltipUtils.getMissingFeatureConfigurationTooltip(utils, entry));
    }

    @NotNull
    @Override
    public <T extends PlacementModifier> TooltipNode getPlacementModifierTooltip(IServerUtils utils, T entry) {
        return placementModifierTooltips.get(entry.getClass())
                .map((e) -> e.apply(utils, entry))
                .orElseGet(() -> PlacementModifierTooltipUtils.getMissingPlacementModifierTooltip(utils, entry));
    }

    @NotNull
    @Override
    public <T extends IntProvider> TooltipBuilder getIntProviderTooltip(IServerUtils utils, T entry) {
        return intProviderTooltips.get(entry.getClass())
                .map((e) -> e.apply(utils, entry))
                .orElseGet(() -> IntProviderTooltipUtils.getMissingIntProviderTooltip(utils, entry));
    }

    @NotNull
    @Override
    public <T extends RuleTest> TooltipBuilder getRuleTestTooltip(IServerUtils utils, T entry) {
        return ruleTestTooltips.get(entry.getClass())
                .map((e) -> e.apply(utils, entry))
                .orElseGet(() -> RuleTestTooltipUtils.getMissingRuleTestTooltip(utils, entry));
    }

    @NotNull
    @Override
    public <T> TooltipBuilder getValueTooltip(IServerUtils utils, @Nullable T value) {
        if (value == null) {
            return TooltipBuilder.empty();
        }

        Class<?> valueClass = value.getClass();

        if (valueClass.isArray()) {
            return TooltipUtils.getArrayTooltip(utils, value);
        } else {
            return valueTooltips.get(valueClass)
                    .map((v) -> v.apply(utils, value))
                    .orElseGet(() -> ValueTooltipUtils.getMissingValueTooltip(utils, value));
        }
    }

    @Override
    public void printRuntimeInfo() {
        super.printRuntimeInfo();
    }
}
