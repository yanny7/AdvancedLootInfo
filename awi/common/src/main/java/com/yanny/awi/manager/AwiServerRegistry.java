package com.yanny.awi.manager;

import com.mojang.logging.LogUtils;
import com.yanny.aci.manager.CoreServerRegistry;
import com.yanny.aci.manager.ManagedRegistry;
import com.yanny.awi.api.*;
import com.yanny.awi.plugin.common.tooltip.*;
import com.yanny.awi.plugin.server.PlacedFeatureUtils;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;

public class AwiServerRegistry extends CoreServerRegistry<Object, ICommonUtils> implements IServerRegistry, IServerUtils, ICommonUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, PlacedFeature, FeatureHolder>> placedFeatures = registerClassKeyed("placed features", false, HashMap::new, null);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, FeatureConfiguration, List<Item>>> itemCollectors = registerClassKeyed("item collectors", false, HashMap::new, null);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, FeatureConfiguration, ITooltipNode>> featureTooltips = registerClassKeyed("feature tooltips", false, HashMap::new, null);

    public AwiServerRegistry(ICommonUtils registry) {
        super(registry);
    }

    @Override
    public <T extends PlacedFeature> void registerFeatureCollector(IServerUtils utils, BiFunction<IServerUtils, T, FeatureHolder> getter) {

    }

    @Override
    public <T extends FeatureConfiguration> void registerItemCollector(IServerUtils utils, BiFunction<IServerUtils, T, List<Item>> getter) {

    }

    @Override
    public <T extends FeatureConfiguration> void registerFeatureTooltip(IServerUtils utils, BiFunction<IServerUtils, T, ITooltipNode> getter) {

    }

    @NotNull
    @Override
    public <T extends PlacedFeature> FeatureHolder getPlacedFeature(IServerUtils utils, T placedFeature) {
        return placedFeatures.get(placedFeature.getClass())
                .map((f) -> f.apply(utils, placedFeature))
                .orElseGet(() -> PlacedFeatureUtils.getMissingPlacedFeature(utils, placedFeature));
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
