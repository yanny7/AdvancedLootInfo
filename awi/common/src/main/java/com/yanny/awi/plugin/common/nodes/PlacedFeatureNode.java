package com.yanny.awi.plugin.common.nodes;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.awi.Utils;
import com.yanny.awi.api.IClientUtils;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.api.ListNode;
import com.yanny.awi.language.Lang;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class PlacedFeatureNode extends ListNode {
    public static final ResourceLocation ID = Utils.modLoc("placed_feature");

    private final TooltipNode tooltip;

    public PlacedFeatureNode(IServerUtils utils, PlacedFeature placedFeature) {
        // -> PlacedFeatureNode
        //   -> ConfiguredFeature:
        //     -> FeatureConfiguration (items)
        //     -> Feature
        //   -> Placement: (conditions)

        ConfiguredFeature<?, ?> configuredFeature = placedFeature.feature().value();
        FeatureConfiguration featureConfiguration = configuredFeature.config(); // values
        Set<Block> blocks = new HashSet<>(utils.collectBlocks(utils, featureConfiguration));

        tooltip = TooltipBuilder.array((b) -> {
            for (PlacementModifier placementModifier : placedFeature.placement()) {
                b.add(utils.getPlacementModifierTooltip(utils, placementModifier));
            }
        }, Lang.Branch.PLACEMENT).build();

        for (Block block : blocks) {
            addChildren(new BlockNode(utils, block));
        }
    }

    public PlacedFeatureNode(IClientUtils utils, FriendlyByteBuf buf) {
        super(utils, buf);
        tooltip = utils.getTooltipCache().getNodeById(buf.readVarInt());
    }

    @Override
    public void encodeNode(IServerUtils utils, FriendlyByteBuf buf) {
        buf.writeVarInt(utils.getTooltipCache().getNodeId(tooltip));
    }

    @NotNull
    @Override
    public TooltipNode getTooltip() {
        return tooltip;
    }

    @NotNull
    @Override
    public ResourceLocation getId() {
        return ID;
    }
}
