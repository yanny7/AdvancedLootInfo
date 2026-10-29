package com.yanny.aci.gametest;

import com.yanny.aci.api.CoreListNode;
import com.yanny.aci.api.ICoreDataNode;
import com.yanny.aci.tooltip.CoreTooltipUtils;
import com.yanny.aci.tooltip.NumberOptions;
import com.yanny.aci.tooltip.TooltipLine;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.aci.tooltip.TooltipStyle;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.stream.Collectors;

public class GoldenRenderer {
    private final boolean isAdvanced;
    private final NumberOptions options;

    public GoldenRenderer(boolean isAdvanced, NumberOptions options) {
        this.isAdvanced = isAdvanced;
        this.options = options;
    }

    @NotNull
    public final String render(ICoreDataNode<?> node) {
        StringBuilder builder = new StringBuilder();

        render(builder, node, 0);
        return builder.toString();
    }

    public final void appendTooltip(StringBuilder builder, String indent, TooltipNode tooltip) {
        for (TooltipLine line : CoreTooltipUtils.toLines(tooltip, 0, isAdvanced, TooltipStyle.DEFAULT, options)) {
            builder.append(indent).append("  | ").append(lineToString(line)).append('\n');
        }
    }

    @Nullable
    protected String describe(ICoreDataNode<?> node) {
        return null;
    }

    protected void appendExtra(StringBuilder builder, String indent, ICoreDataNode<?> node) {
    }

    private void render(StringBuilder builder, ICoreDataNode<?> node, int depth) {
        String indent = "  ".repeat(depth);
        String description = describe(node);

        builder.append(indent).append(node.getId());

        if (description != null) {
            builder.append(' ').append(description);
        }

        builder.append('\n');
        appendTooltip(builder, indent, node.getTooltip());
        appendExtra(builder, indent, node);

        if (node instanceof CoreListNode<?, ?, ?> listNode) {
            for (ICoreDataNode<?> child : listNode.nodes()) {
                render(builder, child, depth + 1);
            }
        }
    }

    @NotNull
    private static String lineToString(TooltipLine line) {
        if (line instanceof TooltipLine.Text(Component component)) {
            return component.getString();
        }

        TooltipLine.Chart chart = (TooltipLine.Chart) line;

        return "  ".repeat(chart.indent()) + "[chart " + chart.series().stream().map(GoldenRenderer::seriesToString).collect(Collectors.joining(" | ")) + "]";
    }

    @NotNull
    private static String seriesToString(TooltipLine.Series series) {
        String heights = series.heights().stream().map((h) -> String.format(Locale.ROOT, "%.4f", h)).collect(Collectors.joining(","));

        return series.first() + ".." + series.last() + " " + series.min().getString() + "-" + series.max().getString() + " base=" + series.base() + " modes=" + series.modes() + " heights=" + heights;
    }
}
