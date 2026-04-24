package com.yanny.awi.plugin.common.nodes;

import com.yanny.awi.Utils;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.api.ITooltipNode;
import com.yanny.awi.api.ListNode;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

public class PlacedFeatureNode extends ListNode {
    public static final ResourceLocation ID = Utils.modLoc("placed_feature");

    public PlacedFeatureNode(IServerUtils utils, GenerationStep.Decoration step, HolderSet<PlacedFeature> features) {
        for (Holder<PlacedFeature> placedFeatureHolder : features) {
            // analyze and merge data by block
            PlacedFeature placedFeature = placedFeatureHolder.value();

            ConfiguredFeature<?, ?> configuredFeature = placedFeature.feature().value();

            FeatureConfiguration featureConfiguration = configuredFeature.config(); // values

            for (PlacementModifier placementModifier : placedFeature.placement()) {
                // conditions
            }
        }
    }

    @Override
    public void encodeNode(IServerUtils utils, RegistryFriendlyByteBuf buf) {

    }

    @Override
    public ITooltipNode getTooltip() {
        return null;
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }
}
