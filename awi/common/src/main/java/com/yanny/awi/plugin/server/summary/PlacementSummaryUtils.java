package com.yanny.awi.plugin.server.summary;

import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.language.Lang;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Orchestrator: walks a placed feature's {@code PlacementModifier} list through the placement propagator
 * registry and merges the per-modifier {@link PlacementContribution}s into one {@link PlacementSummary},
 * then appends it as top-level (un-headed) text lines to a tooltip.
 */
public class PlacementSummaryUtils {
    @NotNull
    public static PlacementSummary summarize(IServerUtils utils, List<PlacementModifier> modifiers, ColumnContext ctx) {
        CountSpan count = null;
        RangeValue chancePercent = null;
        HeightSpan height = null;

        for (PlacementModifier modifier : modifiers) {
            PlacementContribution contribution = utils.getPlacementContribution(utils, modifier, ctx);

            if (contribution.count() != null && count == null) {
                count = contribution.count();
            }
            if (contribution.chancePercent() != null) {
                // multiple rarity filters compound: p = p1 * p2 (percent ⇒ divide by 100)
                chancePercent = (chancePercent == null)
                        ? contribution.chancePercent()
                        : chancePercent.multiply(contribution.chancePercent()).multiply(0.01f);
            }
            if (contribution.height() != null && height == null) {
                height = contribution.height();
            }
        }

        return new PlacementSummary(count, chancePercent, height);
    }

    /**
     * Appends the summary as top-level (un-headed) lines directly onto {@code b} (e.g. the
     * {@code PlacedFeatureNode} branch), so Count/Chance/Height show right at the top of the tooltip.
     */
    public static void appendSummary(TooltipBuilder b, IServerUtils utils, List<PlacementModifier> modifiers, ColumnContext ctx) {
        PlacementSummary summary = summarize(utils, modifiers, ctx);

        if (summary.count() != null) {
            addCount(b, summary.count());
        }
        if (summary.chancePercent() != null) {
            b.add(utils.getValueTooltip(utils, summary.chancePercent().toFloatString() + "%").build(Lang.Value.CHANCE));
        }
        if (summary.height() != null) {
            addHeight(b, utils, summary.height());
        }
    }

    /** Attempts per chunk (feature tries, NOT block count), with the count distribution kind when meaningful. */
    private static void addCount(TooltipBuilder b, CountSpan count) {
        if (count.range().isUnknown()) {
            TooltipBuilder value = TooltipBuilder.value(TooltipBuilder.translate(Lang.Kind.UNKNOWN.singular()));

            if (count.details() != null) {
                value.add(count.details());
            }

            b.add(value.build(Lang.Value.ATTEMPTS_PER_CHUNK));
            return;
        }

        String range = count.range().toIntString();

        if (showsKind(count.kind())) {
            b.add(TooltipBuilder.value(range, TooltipBuilder.translate(kindKey(count.kind()))).build(Lang.Value.ATTEMPTS_PER_CHUNK_DIST));
        } else {
            b.add(TooltipBuilder.value(range).build(Lang.Value.ATTEMPTS_PER_CHUNK));
        }
    }

    /** One line: full range, distribution kind, and the "most likely" band (only when it narrows the range). */
    private static void addHeight(TooltipBuilder b, IServerUtils utils, HeightSpan height) {
        if (height.heightmap() != null) {
            b.add(utils.getValueTooltip(utils, height.heightmap()).build(Lang.Value.HEIGHTMAP));
            return;
        }
        if (height.range().isUnknown()) {
            b.add(TooltipBuilder.value(TooltipBuilder.translate(Lang.Kind.UNKNOWN.singular())).build(Lang.Value.HEIGHT));
            return;
        }

        String range = height.range().toIntString();

        if (showsKind(height.kind())) {
            String kind = TooltipBuilder.translate(kindKey(height.kind()));

            if (height.bestBand() != null && !isSameRange(height.bestBand(), height.range())) {
                b.add(TooltipBuilder.value(range, kind, height.bestBand().toIntString()).build(Lang.Value.HEIGHT_DIST_BAND));
            } else {
                b.add(TooltipBuilder.value(range, kind).build(Lang.Value.HEIGHT_DIST));
            }
        } else {
            b.add(TooltipBuilder.value(range).build(Lang.Value.HEIGHT));
        }
    }

    @NotNull
    private static String kindKey(Kind kind) {
        return Lang.Kind.valueOf(kind.name()).singular();
    }

    private static boolean showsKind(Kind kind) {
        // CONSTANT is obvious from a single value; UNKNOWN / heightmap-relative carry no useful shape
        return switch (kind) {
            case UNIFORM, BIASED_TO_BOTTOM, VERY_BIASED_TO_BOTTOM, TRAPEZOID, CLAMPED, CLAMPED_NORMAL, WEIGHTED -> true;
            default -> false;
        };
    }

    private static boolean isSameRange(RangeValue a, RangeValue b) {
        return a.min() == b.min() && a.max() == b.max();
    }
}
