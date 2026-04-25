package com.yanny.awi.manager;

import com.mojang.logging.LogUtils;
import com.yanny.aci.manager.CoreServerRegistry;
import com.yanny.awi.api.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.List;

public class AwiServerRegistry extends CoreServerRegistry<Object, ICommonUtils> implements IServerRegistry, IServerUtils, ICommonUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    public AwiServerRegistry(ICommonUtils registry) {
        super(registry);
    }

    public void clearData() {
    }

    public void printRegistrationInfo() {
//        LOGGER.info("Registered {} loot modifiers", lootModifierMap.size());
    }

    @NotNull
    @Override
    public <T extends PlacedFeature> FeatureHolder registerFeatureCollector(IServerUtils utils, T placedFeature) {
        return null;
    }

    @NotNull
    @Override
    public <T extends FeatureConfiguration> List<Item> registerItemCollector(IServerUtils utils, T configuredFeature) {
        return List.of();
    }

    @NotNull
    @Override
    public <T extends FeatureConfiguration> ITooltipNode registerFeatureTooltip(IServerUtils utils, T configuredFeature) {
        return null;
    }
}
