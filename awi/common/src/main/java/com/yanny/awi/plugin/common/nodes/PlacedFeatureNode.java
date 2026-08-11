package com.yanny.awi.plugin.common.nodes;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.awi.Utils;
import com.yanny.awi.api.IClientUtils;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.api.ListNode;
import com.yanny.awi.language.Lang;
import com.yanny.awi.plugin.server.FeatureConfigurationCollectorUtils;
import com.yanny.awi.plugin.server.summary.ColumnContext;
import com.yanny.awi.plugin.server.summary.PlacementSummaryUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Set;

public class PlacedFeatureNode extends ListNode {
    public static final Identifier ID = Utils.modLoc("placed_feature");

    private final TooltipNode tooltip;
    @Nullable
    private final Identifier featureId;

    public PlacedFeatureNode(IServerUtils utils, PlacedFeature placedFeature, ColumnContext columnContext, @Nullable Identifier featureId) {
        // -> PlacedFeatureNode
        //   -> Count / Chance / Height (top-level summary)
        //   -> ConfiguredFeature:
        //     -> FeatureConfiguration (items)
        //     -> Feature
        //   -> Placement: (conditions)

        this.featureId = featureId;

        ConfiguredFeature<?, ?> configuredFeature = placedFeature.feature().value();
        FeatureConfiguration featureConfiguration = configuredFeature.config(); // values
        Set<Either<Block, TagKey<Block>>> blocks = new LinkedHashSet<>(FeatureConfigurationCollectorUtils.collectConfiguredFeatureBlocks(utils, configuredFeature));

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

        for (Either<Block, TagKey<Block>> block : blocks) {
            block.ifLeft((b) -> {
                if (b != Blocks.AIR && b != Blocks.CAVE_AIR && b != Blocks.VOID_AIR && b != Blocks.BARRIER) {
                    addChildren(new BlockNode(utils, b));
                }
            }).ifRight((tag) -> addChildren(new BlockNode(utils, tag, utils.getValueTooltip(utils, tag).build(Lang.Value.TAG))));
        }
    }

    public PlacedFeatureNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        super(utils, buf);
        tooltip = utils.getTooltipCache().getNodeById(buf.readVarInt());
        featureId = buf.readBoolean() ? buf.readIdentifier() : null;
    }

    @Override
    public void encodeNode(IServerUtils utils, RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(utils.getTooltipCache().getNodeId(tooltip));
        buf.writeBoolean(featureId != null);

        if (featureId != null) {
            buf.writeIdentifier(featureId);
        }
    }

    @Nullable
    public Identifier getFeatureId() {
        return featureId;
    }

    @NotNull
    @Override
    public TooltipNode getTooltip() {
        return tooltip;
    }

    @NotNull
    @Override
    public Identifier getId() {
        return ID;
    }
}
