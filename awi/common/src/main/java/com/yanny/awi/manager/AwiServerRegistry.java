package com.yanny.awi.manager;

import com.yanny.aci.manager.ClassKeyedMap;
import com.yanny.aci.manager.CoreServerRegistry;
import com.yanny.aci.manager.ManagedRegistry;
import com.yanny.awi.api.*;
import com.yanny.awi.plugin.common.tooltip.*;
import com.yanny.awi.plugin.server.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
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

public class AwiServerRegistry extends CoreServerRegistry<Object, ICommonUtils, ITooltipNode, IServerUtils, IKeyTooltipNode> implements IServerRegistry, IServerUtils, ICommonUtils {
    // collectors
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, FeatureConfiguration, List<Item>>> itemCollectors = registerClassKeyed("item collectors", false, HashMap::new, null);
    // tooltips
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, FeatureConfiguration, ITooltipNode>> featureTooltips = registerClassKeyed("feature tooltips", false, HashMap::new, null);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, PlacementModifier, ITooltipNode>> placementModifierTooltips = registerClassKeyed("placement modifier tooltips", true, HashMap::new, BuiltInRegistries.PLACEMENT_MODIFIER_TYPE);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, IntProvider, IKeyTooltipNode>> intProviderTooltips = registerClassKeyed("int provider tooltips", true, HashMap::new, BuiltInRegistries.INT_PROVIDER_TYPE);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, RuleTest, IKeyTooltipNode>> ruleTestTooltips = registerClassKeyed("rule test tooltips", true, HashMap::new, BuiltInRegistries.RULE_TEST);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, Object, IKeyTooltipNode>> valueTooltips = registerClassKeyed("value tooltips", true, ClassKeyedMap::new, null);

    public AwiServerRegistry(ICommonUtils registry) {
        super(registry);
    }

    @Override
    public <T extends FeatureConfiguration> void registerItemCollector(Class<T> type, BiFunction<IServerUtils, T, List<Item>> getter) {
        //noinspection unchecked
        itemCollectors.put(type, (u, t) -> getter.apply(u, (T) t));
    }

    @Override
    public <T extends FeatureConfiguration> void registerFeatureTooltip(Class<T> type, BiFunction<IServerUtils, T, ITooltipNode> getter) {
        //noinspection unchecked
        featureTooltips.put(type, (u, t) -> getter.apply(u, (T) t));
    }

    @Override
    public <T extends PlacementModifier> void registerPlacementModifierTooltip(Class<T> type, BiFunction<IServerUtils, T, ITooltipNode> getter) {
        //noinspection unchecked
        placementModifierTooltips.put(type, (u, t) -> getter.apply(u, (T) t));
    }

    @Override
    public <T extends IntProvider> void registerIntProviderTooltip(Class<T> type, BiFunction<IServerUtils, T, IKeyTooltipNode> getter) {
        //noinspection unchecked
        intProviderTooltips.put(type, (u, t) -> getter.apply(u, (T) t));
    }

    @Override
    public <T extends RuleTest> void registerRuleTestTooltip(Class<T> type, BiFunction<IServerUtils, T, IKeyTooltipNode> getter) {
        //noinspection unchecked
        ruleTestTooltips.put(type, (u, t) -> getter.apply(u, (T) t));
    }

    @Override
    public <T> void registerValueTooltip(Class<T> type, BiFunction<IServerUtils, T, IKeyTooltipNode> getter) {
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
    public <T extends FeatureConfiguration> ITooltipNode getFeatureTooltip(IServerUtils utils, T entry) {
        return featureTooltips.get(entry.getClass())
                .map((e) -> e.apply(utils, entry))
                .orElseGet(() -> FeatureConfigurationTooltipUtils.getMissingFeatureConfigurationTooltip(utils, entry));
    }

    @NotNull
    @Override
    public <T extends PlacementModifier> ITooltipNode getPlacementModifierTooltip(IServerUtils utils, T entry) {
        return placementModifierTooltips.get(entry.getClass())
                .map((e) -> e.apply(utils, entry))
                .orElseGet(() -> PlacementModifierTooltipUtils.getMissingPlacementModifierTooltip(utils, entry));
    }

    @NotNull
    @Override
    public <T extends IntProvider> IKeyTooltipNode getIntProviderTooltip(IServerUtils utils, T entry) {
        return intProviderTooltips.get(entry.getClass())
                .map((e) -> e.apply(utils, entry))
                .orElseGet(() -> IntProviderTooltipUtils.getMissingIntProviderTooltip(utils, entry));
    }

    @NotNull
    @Override
    public <T extends RuleTest> IKeyTooltipNode getRuleTestTooltip(IServerUtils utils, T entry) {
        return ruleTestTooltips.get(entry.getClass())
                .map((e) -> e.apply(utils, entry))
                .orElseGet(() -> RuleTestTooltipUtils.getMissingRuleTestTooltip(utils, entry));
    }

    @NotNull
    @Override
    public <T> IKeyTooltipNode getValueTooltip(IServerUtils utils, @Nullable T value) {
        if (value == null) {
            return EmptyTooltipNode.empty();
        }

        Class<?> valueClass = value.getClass();

        if (valueClass.isArray()) {
            return TooltipUtils.getArrayTooltip(utils, value);
        } else {
            return valueTooltips.get(valueClass)
                    .map((v) -> v.apply(utils, value))
                    .orElseGet(() -> ErrorTooltipNode.error("[" + valueClass.getName() + "]"));
        }
    }

    @NotNull
    @Override
    public ITooltipNode buildTooltip(IKeyTooltipNode keyTooltipNode) {
        if (keyTooltipNode instanceof BranchTooltipNode.Builder builder) {
            return builder.build("aci.util.values");
        } else if (keyTooltipNode instanceof ErrorTooltipNode.Builder builder) {
            return builder.build();
        } else if (keyTooltipNode instanceof ArrayTooltipNode.Builder builder) {
            return builder.build();
        } else if (keyTooltipNode instanceof EmptyTooltipNode.Builder builder) {
            return builder.build();
        } else {
            return keyTooltipNode.build("aci.util.null");
        }
    }

    @NotNull
    @Override
    public IKeyTooltipNode getBranchNode() {
        return BranchTooltipNode.branch();
    }

    @NotNull
    @Override
    public IKeyTooltipNode getBranchNode(boolean isAdvancedTooltip) {
        return BranchTooltipNode.branch(isAdvancedTooltip);
    }

    @NotNull
    @Override
    public IKeyTooltipNode getValueNode(Object... value) {
        return ValueTooltipNode.value(value);
    }

    @NotNull
    @Override
    public IKeyTooltipNode getKeyValueNode(Object key, Object value) {
        return ValueTooltipNode.keyValue(key, value);
    }

    @NotNull
    @Override
    public IKeyTooltipNode getComponentNode(Component... values) {
        return ComponentTooltipNode.values(values);
    }

    @NotNull
    @Override
    public ITooltipNode getLiteralNode(String translatable) {
        return LiteralTooltipNode.translatable(translatable);
    }

    @NotNull
    @Override
    public IKeyTooltipNode getEmptyNode() {
        return EmptyTooltipNode.empty();
    }

    @NotNull
    @Override
    public IKeyTooltipNode getErrorNode(String error) {
        return ErrorTooltipNode.error(error);
    }

    @Override
    public void printRuntimeInfo() {
        super.printRuntimeInfo();
        ArrayTooltipNode.logCacheStatistics(this);
        BranchTooltipNode.logCacheStatistics(this);
        ComponentTooltipNode.logCacheStatistics(this);
        LiteralTooltipNode.logCacheStatistics(this);
        ValueTooltipNode.logCacheStatistics(this);
    }
}
