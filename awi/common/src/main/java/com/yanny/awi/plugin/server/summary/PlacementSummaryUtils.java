package com.yanny.awi.plugin.server.summary;

import com.yanny.aci.CommonLogUtils;
import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.awi.Utils;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.language.Lang;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class PlacementSummaryUtils {
    private static final Logger LOGGER = CommonLogUtils.getLogger(Utils.MOD_ID);

    @NotNull
    public static PlacementSummary summarize(IServerUtils utils, List<PlacementModifier> modifiers, ColumnContext ctx) {
        NumberExpr count = null;
        List<TooltipNode> countConditions = List.of();
        TooltipNode countDetails = null;
        NumberExpr chance = null;
        List<TooltipNode> chanceConditions = new ArrayList<>();
        HeightSpan height = null;

        for (PlacementModifier modifier : modifiers) {
            PlacementContribution contribution = utils.getPlacementContribution(utils, modifier, ctx);

            if (contribution.count() != null && count == null) {
                count = contribution.count();
                countConditions = contribution.countConditions();
                countDetails = contribution.countDetails();
            }
            if (contribution.chance() != null) {
                NumberExpr shifted = contribution.chance().shiftConditions(chanceConditions.size());

                chance = chance == null ? shifted : NumberExpr.mul(chance, shifted);
                chanceConditions.addAll(contribution.chanceConditions());
            }
            if (contribution.height() != null && height == null) {
                height = contribution.height();
            }
        }

        return new PlacementSummary(count, countConditions, countDetails, chance, chanceConditions, height);
    }

    public static void appendSummary(TooltipBuilder b, IServerUtils utils, List<PlacementModifier> modifiers, ColumnContext ctx) {
        PlacementSummary summary;

        try {
            summary = summarize(utils, modifiers, ctx);
        } catch (Throwable e) {
            LOGGER.warn("Failed to summarize placement: {}", e.getMessage(), e);
            return;
        }

        if (summary.count() != null) {
            TooltipBuilder count = TooltipBuilder.number(summary.count(), summary.countConditions());

            if (summary.countDetails() != null) {
                count.add(summary.countDetails());
            }

            b.add(count.build(Lang.Value.ATTEMPTS_PER_CHUNK));
        }
        if (summary.chance() != null) {
            TooltipBuilder chance = TooltipBuilder.percent(summary.chance());

            summary.chanceConditions().forEach(chance::add);
            b.add(chance.build(Lang.Value.CHANCE));
        }
        if (summary.height() != null) {
            HeightSpan height = summary.height();

            if (height.heightmap() != null) {
                b.add(utils.getValueTooltip(utils, height.heightmap()).build(Lang.Value.HEIGHT));
            } else if (height.height() != null) {
                b.add(TooltipBuilder.number(height.height(), height.conditions()).build(Lang.Value.HEIGHT));
            }
        }
    }
}
