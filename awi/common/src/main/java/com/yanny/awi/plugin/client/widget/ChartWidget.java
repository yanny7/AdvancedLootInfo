package com.yanny.awi.plugin.client.widget;

import com.yanny.aci.api.IWidget;
import com.yanny.aci.api.NumberText;
import com.yanny.aci.api.RelativeRect;
import com.yanny.aci.api.WidgetDirection;
import com.yanny.aci.compatibility.GuiGraphics;
import com.yanny.aci.tooltip.TooltipLine;
import com.yanny.aci.tooltip.TooltipStyle;
import com.yanny.awi.compatibility.BlockGeneration;
import com.yanny.awi.compatibility.BlockGenerationChart;
import com.yanny.awi.compatibility.GenericUtils;
import com.yanny.awi.language.Lang;
import com.yanny.awi.plugin.client.TooltipUtils;
import com.yanny.awi.plugin.common.nodes.PlacedFeatureNode;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.IntStream;

public class ChartWidget implements IWidget {
    private static final int[] TICK_STEPS = {8, 16, 32, 64, 128, 256, 512};
    private static final int MIN_TICK_SPACING = 10;
    private static final int AXIS_GAP = 2;
    private static final int COLUMN_GAP = 4;
    private static final int ROW_GAP = 4;
    private static final int MAX_COLUMNS = 3;

    private final RelativeRect rect;
    private final BlockGenerationChart chart;
    private final Map<BlockGeneration.Source, Integer> colors;
    private final int labelWidth;
    private final int tickStep;
    private final int columnWidth;
    private final List<Cell> cells = new ArrayList<>();

    public ChartWidget(RelativeRect rect, int width, BlockGenerationChart chart, Map<BlockGeneration.Source, Integer> colors) {
        Font font = Minecraft.getInstance().font;
        int topY = chart.binMinY(chart.bins());
        int perRow = Math.min(chart.columns().size(), MAX_COLUMNS);

        this.rect = rect;
        this.chart = chart;
        this.colors = colors;
        labelWidth = Math.max(font.width(String.valueOf(chart.minY())), font.width(String.valueOf(topY))) + AXIS_GAP;
        tickStep = IntStream.of(TICK_STEPS).filter((s) -> s / chart.binSize() >= MIN_TICK_SPACING).findFirst().orElse(TICK_STEPS[TICK_STEPS.length - 1]);
        columnWidth = (width - labelWidth - COLUMN_GAP * (perRow - 1)) / perRow;

        int y = 0;

        for (int i = 0; i < chart.columns().size(); i++) {
            if (i > 0 && i % perRow == 0) {
                y += rowHeight() + ROW_GAP;
            }

            cells.add(new Cell(chart.columns().get(i), labelWidth + (i % perRow) * (columnWidth + COLUMN_GAP), y));
        }

        rect.setDimensions(width, y + rowHeight());
    }

    @NotNull
    @Override
    public RelativeRect getRect() {
        return rect;
    }

    @NotNull
    @Override
    public WidgetDirection getDirection() {
        return WidgetDirection.VERTICAL;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        Font font = Minecraft.getInstance().font;
        Locale locale = TooltipUtils.getNumberOptions().locale();

        for (Cell cell : cells) {
            int left = rect.getX() + cell.x;
            int right = left + columnWidth;
            int top = barTop(cell);
            int bottom = top + chart.bins();
            int hovered = hoveredBin(cell, mouseX, mouseY);
            Component header = LabelWidget.fit(font, header(cell.column), columnWidth);

            guiGraphics.drawString(font, header, left + (columnWidth - font.width(header)) / 2, rect.getY() + cell.y + 1, LabelWidget.COLOR, false);
            guiGraphics.fill(left, top, right, bottom, 0x30000000);

            for (int y = Math.floorDiv(chart.minY(), tickStep) * tickStep; y < chart.binMinY(chart.bins()); y += tickStep) {
                if (y < chart.minY()) {
                    continue;
                }

                int row = bottom - 1 - (y - chart.minY()) / chart.binSize();

                guiGraphics.fill(left, row, right, row + 1, 0x30000000);

                if (cell.x == labelWidth) {
                    String label = String.valueOf(y);

                    guiGraphics.drawString(font, label, left - AXIS_GAP - font.width(label), row - 4, LabelWidget.COLOR, false);
                }
            }

            for (int bin = 0; bin < chart.bins(); bin++) {
                int row = bottom - 1 - bin;

                if (cell.column.terrain()) {
                    renderLanes(guiGraphics, cell.column, bin, left, row);
                } else {
                    renderStack(guiGraphics, cell.column, bin, left, row);
                }

                if (bin == hovered) {
                    guiGraphics.fill(left, row, right, row + 1, 0x80FFFFFF);
                }
            }

            guiGraphics.fill(left - 1, top, left, bottom + 1, 0xFF404040);

            if (!cell.column.terrain()) {
                String max = NumberText.formatNumber(cell.column.max(), locale);

                guiGraphics.drawString(font, "0", left, bottom + AXIS_GAP, LabelWidget.COLOR, false);
                guiGraphics.drawString(font, max, right - font.width(max), bottom + AXIS_GAP, LabelWidget.COLOR, false);
            }
        }
    }

    private void renderLanes(GuiGraphics guiGraphics, BlockGenerationChart.Column column, int bin, int left, int row) {
        List<BlockGenerationChart.Series> lanes = column.series();

        for (int i = 0; i < lanes.size(); i++) {
            if (lanes.get(i).values()[bin] > 0) {
                guiGraphics.fill(left + i * columnWidth / lanes.size(), row, left + (i + 1) * columnWidth / lanes.size(), row + 1, color(lanes.get(i)));
            }
        }
    }

    private void renderStack(GuiGraphics guiGraphics, BlockGenerationChart.Column column, int bin, int left, int row) {
        double cumulative = 0;

        for (BlockGenerationChart.Series series : column.series()) {
            double value = series.values()[bin];
            int x0 = left + (int) Math.round(cumulative / column.max() * columnWidth);
            int x1 = left + (int) Math.round((cumulative + value) / column.max() * columnWidth);

            guiGraphics.fill(x0, row, x1, row + 1, color(series));
            cumulative += value;
        }
    }

    private int color(BlockGenerationChart.Series series) {
        return colors.getOrDefault(series.source(), 0xFF808080);
    }

    @NotNull
    @Override
    public List<TooltipLine> getTooltipLines(int mouseX, int mouseY) {
        for (Cell cell : cells) {
            int left = rect.getX() + cell.x;

            if (mouseX < left || mouseX >= left + columnWidth) {
                continue;
            }

            int bin = hoveredBin(cell, mouseX, mouseY);

            if (bin >= 0) {
                return binTooltip(cell.column, bin);
            } else if (mouseY >= rect.getY() + cell.y && mouseY < barTop(cell)) {
                List<TooltipLine> lines = new ArrayList<>();

                lines.add(TooltipLine.text(header(cell.column)));
                cell.column.biomes().forEach((b) -> lines.add(TooltipLine.text(GenericUtils.getBiomeTitle(b).copy().withStyle(ChatFormatting.GRAY))));
                return lines;
            }
        }

        return List.of(
                TooltipLine.text(title()),
                TooltipLine.text(Component.translatable(Lang.BlockGeneration.CHART_EXCLUDES.singular()).withStyle(ChatFormatting.GRAY))
        );
    }

    @NotNull
    private Component title() {
        boolean terrainOnly = chart.columns().stream().allMatch(BlockGenerationChart.Column::terrain);

        return Component.translatable((terrainOnly ? Lang.BlockGeneration.CHART_TERRAIN : Lang.BlockGeneration.CHART).singular());
    }

    @NotNull
    private static Component header(BlockGenerationChart.Column column) {
        if (column.terrain()) {
            return Component.translatable(Lang.GenerationStep.BASE_TERRAIN.singular());
        }

        return BlockGenerationWidget.biomesLabel(column.biomes(), column.allBiomes());
    }

    @NotNull
    private List<TooltipLine> binTooltip(BlockGenerationChart.Column column, int bin) {
        Locale locale = TooltipUtils.getNumberOptions().locale();
        int from = chart.binMinY(bin);
        List<TooltipLine> lines = new ArrayList<>();

        lines.add(TooltipLine.text(Component.translatable(Lang.BlockGeneration.CHART_RANGE.singular(), from, from + chart.binSize() - 1)));

        if (column.terrain()) {
            column.series().stream()
                    .filter((s) -> s.values()[bin] > 0)
                    .forEach((s) -> {
                        lines.add(TooltipLine.text(square(s).append(Component.translatable(Lang.GenerationStep.BASE_TERRAIN.singular()))));
                        lines.add(branch(Component.translatable(Lang.BlockGeneration.BIOMES_LIST.singular()),
                                BlockGenerationWidget.biomesLabel(s.biomes(), s.biomes().size() == chart.biomeCount())));
                    });
            return lines;
        }

        column.series().stream()
                .filter((s) -> s.values()[bin] > 0)
                .sorted(Comparator.comparingDouble((BlockGenerationChart.Series s) -> -s.values()[bin]))
                .forEach((s) -> addSeriesLines(lines, s, s.values()[bin], locale));
        lines.add(TooltipLine.text(Component.translatable(Lang.BlockGeneration.CHART_TOTAL.singular(), NumberText.formatNumber(column.total(bin), locale))));
        return lines;
    }

    private void addSeriesLines(List<TooltipLine> lines, BlockGenerationChart.Series series, double value, Locale locale) {
        PlacedFeatureNode feature = series.source().feature();
        String name = feature != null && feature.getFeatureId() != null ? feature.getFeatureId().toString() : "?";
        PlacedFeatureNode.Stats stats = feature != null ? feature.getStats() : null;

        lines.add(TooltipLine.text(square(series).append(name)));
        lines.add(branch(Component.translatable(Lang.BlockGeneration.CHART.singular()), Component.literal(NumberText.formatNumber(value, locale))));

        if (stats != null && stats.veinSize() > 0) {
            lines.add(branch(Component.translatable(Lang.BlockGeneration.CHART_VEIN.singular()), Component.literal(String.valueOf(stats.veinSize()))));

            if (stats.airDiscard() > 0) {
                String percent = NumberText.formatNumber(stats.airDiscard() * 100.0, locale) + "%";

                lines.add(branch(Component.translatable(Lang.BlockGeneration.CHART_AIR.singular()), Component.literal(percent)));
            }
        }
    }

    @NotNull
    private MutableComponent square(BlockGenerationChart.Series series) {
        int color = color(series);

        return Component.empty().append(Component.literal("■ ").withStyle((s) -> s.withColor(TextColor.fromRgb(color & 0xFFFFFF))));
    }

    @NotNull
    private static TooltipLine branch(Component key, Component value) {
        TooltipStyle style = TooltipUtils.getStyle();

        return TooltipLine.text(Component.literal("  ").append(Component.literal("-> ").withStyle(style.branch())).append(key.copy().withStyle(style.text()))
                .append(Component.literal(": ").withStyle(style.text())).append(value.copy().withStyle(style.value())));
    }

    private int rowHeight() {
        return LabelWidget.HEIGHT + chart.bins() + AXIS_GAP + LabelWidget.HEIGHT;
    }

    private int barTop(Cell cell) {
        return rect.getY() + cell.y + LabelWidget.HEIGHT;
    }

    private int hoveredBin(Cell cell, int mouseX, int mouseY) {
        int left = rect.getX() + cell.x;
        int row = mouseY - barTop(cell);

        if (mouseX < left || mouseX >= left + columnWidth || row < 0 || row >= chart.bins()) {
            return -1;
        }

        return chart.bins() - 1 - row;
    }

    private record Cell(BlockGenerationChart.Column column, int x, int y) {}
}
