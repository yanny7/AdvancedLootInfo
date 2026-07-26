package com.yanny.awi.plugin.common.nodes;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.awi.Utils;
import com.yanny.awi.api.IClientUtils;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.api.ListNode;
import com.yanny.awi.language.Lang;
import com.yanny.awi.plugin.server.summary.ColumnContext;
import com.yanny.awi.plugin.server.summary.PlacementSummaryUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class PlacedFeatureNode extends ListNode {
    public static final ResourceLocation ID = Utils.modLoc("placed_feature");

    private final TooltipNode tooltip;
    @Nullable
    private final ResourceLocation featureId;

    public PlacedFeatureNode(IServerUtils utils, PlacedFeature placedFeature, ColumnContext columnContext, @Nullable ResourceLocation featureId) {
        // -> PlacedFeatureNode
        //   -> Count / Chance / Height (top-level summary)
        //   -> ConfiguredFeature:
        //     -> FeatureConfiguration (items)
        //     -> Feature
        //   -> Placement: (conditions)

        this.featureId = featureId;

        ConfiguredFeature<?, ?> configuredFeature = placedFeature.feature().value();
        FeatureConfiguration featureConfiguration = configuredFeature.config(); // values
        Set<Block> blocks = new HashSet<>(utils.collectBlocks(utils, featureConfiguration));

        tooltip = TooltipBuilder.branch((b) -> {
            PlacementSummaryUtils.appendSummary(b, utils, placedFeature.placement(), columnContext);

            b.add(TooltipBuilder.array((c) -> {
                c.add(utils.getValueTooltip(utils, configuredFeature.feature()).build(Lang.Value.FEATURE));
                c.add(utils.getValueTooltip(utils, featureConfiguration));
                c.isAdvancedTooltip();
            }, Lang.Branch.CONFIGURED_FEATURE));

            b.add(TooltipBuilder.array((c) -> {
                for (PlacementModifier placementModifier : placedFeature.placement()) {
                    c.add(utils.getPlacementModifierTooltip(utils, placementModifier));
                }

                c.isAdvancedTooltip();
            }, Lang.Branch.PLACEMENT));
        }).build();

        for (Block block : blocks) {
            addChildren(new BlockNode(utils, block));
        }
    }

    public PlacedFeatureNode(IClientUtils utils, FriendlyByteBuf buf) {
        super(utils, buf);
        tooltip = utils.getTooltipCache().getNodeById(buf.readVarInt());
        featureId = buf.readBoolean() ? buf.readResourceLocation() : null;
    }

    @Override
    public void encodeNode(IServerUtils utils, FriendlyByteBuf buf) {
        buf.writeVarInt(utils.getTooltipCache().getNodeId(tooltip));
        buf.writeBoolean(featureId != null);

        if (featureId != null) {
            buf.writeResourceLocation(featureId);
        }
    }

    @Nullable
    public ResourceLocation getFeatureId() {
        return featureId;
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
