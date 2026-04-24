package com.yanny.awi.manager;

import com.mojang.logging.LogUtils;
import com.yanny.aci.manager.CoreServerRegistry;
import com.yanny.awi.api.FeatureHolder;
import com.yanny.awi.api.IServerRegistry;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.api.ITooltipNode;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.slf4j.Logger;

import java.util.List;

public class AwiServerRegistry extends CoreServerRegistry implements IServerRegistry, IServerUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    public void clearData() {
    }

    public void printRegistrationInfo() {
//        LOGGER.info("Registered {} loot modifiers", lootModifierMap.size());
    }

    @Override
    public <T extends PlacedFeature> FeatureHolder registerFeatureCollector(IServerUtils utils, T placedFeature) {
        return null;
    }

    @Override
    public <T extends FeatureConfiguration> List<Item> registerItemCollector(IServerUtils utils, T configuredFeature) {
        return List.of();
    }

    @Override
    public <T extends FeatureConfiguration> ITooltipNode registerFeatureTooltip(IServerUtils utils, T configuredFeature) {
        return null;
    }
}
