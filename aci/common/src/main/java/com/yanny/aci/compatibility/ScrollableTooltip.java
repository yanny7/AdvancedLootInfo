package com.yanny.aci.compatibility;

import com.yanny.aci.tooltip.TooltipLine;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ScrollableTooltip implements TooltipComponent, ClientTooltipComponent {
    private static final int LINE_HEIGHT = TooltipLine.LINE_HEIGHT;
    private static final int SCREEN_MARGIN = 10;
    private static final int WINDOW_MARGIN = 8;
    private static final int TITLE_GAP = 2;
    private static final int SCROLL_LINES = 3;
    private static final int SCROLL_STEP_DIVISOR = 8;
    private static final int SCROLLBAR_GAP = 3;
    private static final int SCROLLBAR_WIDTH = 1;
    private static final int TRACK_COLOR = 0x40FFFFFF;
    private static final int THUMB_COLOR = 0xFFA0A0A0;
    private static final int BAR_COLOR = 0xFF707070;
    private static final int MODE_BAR_COLOR = 0xFFE0E0E0;
    private static final int AXIS_COLOR = 0x40FFFFFF;
    private static final int DIM_PERCENT = 50;
    private static final int PANEL_GAP = 6;
    private static final int LABEL_GAP = 4;
    private static final int LABEL_SPACING = 1;
    private static final long ACTIVE_MILLIS = 100;

    private static List<ClientTooltipComponent> renderedComponents = List.of();
    private static List<TooltipLine> activeLines = List.of();
    private static int offset = 0;
    private static int overflow = 0;
    private static int visibleLines = 0;
    private static long lastRender = 0;

    private final List<TooltipLine> lines;
    private Layout layout;

    public ScrollableTooltip(List<TooltipLine> lines) {
        this.lines = lines;
    }

    public static void setRenderedComponents(List<ClientTooltipComponent> components) {
        renderedComponents = components;
    }

    public static boolean onMouseScrolled(double scrollDeltaY) {
        if (overflow == 0 || Util.getMillis() - lastRender > ACTIVE_MILLIS) {
            return false;
        }

        int step = Mth.clamp(visibleLines / SCROLL_STEP_DIVISOR, 1, SCROLL_LINES);

        offset = Mth.clamp(offset - (int) Math.signum(scrollDeltaY) * step, 0, overflow);
        return true;
    }

    @Override
    public int getHeight(Font font) {
        return getLayout(font).visibleLines() * LINE_HEIGHT;
    }

    @Override
    public int getWidth(Font font) {
        Layout layout = getLayout(font);

        return layout.visibleLines() < layout.totalLines() ? layout.contentWidth() + SCROLLBAR_GAP + SCROLLBAR_WIDTH : layout.contentWidth();
    }

    @Override
    public void extractImage(Font font, int x, int y, int ignoredWidth, int ignoredHeight, GuiGraphicsExtractor guiGraphics) {
        Layout layout = getLayout(font);
        int visible = layout.visibleLines();
        int totalLines = layout.totalLines();
        long now = Util.getMillis();

        if (now - lastRender > ACTIVE_MILLIS || !lines.equals(activeLines)) {
            activeLines = lines;
            offset = 0;
        }

        overflow = totalLines - visible;
        visibleLines = visible;
        offset = Math.min(offset, overflow);
        lastRender = now;

        int position = 0;

        for (int i = 0; i < lines.size(); i++) {
            TooltipLine line = lines.get(i);
            int lineCount = layout.lineCounts()[i];
            int first = Math.max(position, offset);
            int last = Math.min(position + lineCount, offset + visible);

            if (first < last) {
                int top = y + (position - offset) * LINE_HEIGHT;

                if (line instanceof TooltipLine.Chart chart) {
                    renderChart(font, guiGraphics, chart, layout.charts().get(i), x, top, y + (first - offset) * LINE_HEIGHT, y + (last - offset) * LINE_HEIGHT);
                } else {
                    List<FormattedCharSequence> rows = layout.text().get(i);

                    for (int row = first - position; row < last - position; row++) {
                        guiGraphics.text(font, rows.get(row), x, top + row * LINE_HEIGHT, -1);
                    }
                }
            }

            position += lineCount;
        }

        if (overflow > 0) {
            int height = visible * LINE_HEIGHT;
            int trackX = x + layout.contentWidth() + SCROLLBAR_GAP;
            int thumbHeight = Math.max(LINE_HEIGHT, height * visible / totalLines);
            int thumbY = y + (height - thumbHeight) * offset / overflow;

            guiGraphics.fill(trackX, y, trackX + SCROLLBAR_WIDTH, y + height, TRACK_COLOR);
            guiGraphics.fill(trackX, thumbY, trackX + SCROLLBAR_WIDTH, thumbY + thumbHeight, THUMB_COLOR);
        }
    }

    private static void renderChart(Font font, GuiGraphicsExtractor guiGraphics, TooltipLine.Chart chart, ChartLayout layout, int x, int top, int clipTop,
                                    int clipBottom) {
        int left = x + indentWidth(font, chart);

        for (int s = 0; s < chart.series().size(); s++) {
            Panel panel = layout.panels().get(s);
            int panelTop = top + panel.row() * TooltipLine.Chart.PANEL_LINES * LINE_HEIGHT;

            renderPanel(font, guiGraphics, chart, chart.series().get(s), left + panel.x(), panelTop, panel.width(), clipTop, clipBottom);
        }
    }

    private static void renderPanel(Font font, GuiGraphicsExtractor guiGraphics, TooltipLine.Chart chart, TooltipLine.Series series, int left, int top,
                                    int panelWidth, int clipTop, int clipBottom) {
        int width = chart.columnWidth();
        int barsHeight = TooltipLine.Chart.BAR_LINES * LINE_HEIGHT;
        int bottom = top + barsHeight - 1;
        int maxBar = barsHeight - 2;
        int gap = width > 2 ? 1 : 0;

        for (int c = series.first(); c <= series.last(); c++) {
            int barHeight = (int) Math.round(series.heights().get(c) * maxBar);
            int columnLeft = left + (c - series.first()) * width;
            boolean mode = series.modes().get(c);
            int color;

            if (series.base()) {
                color = mode ? MODE_BAR_COLOR : BAR_COLOR;
            } else {
                color = mode ? series.color() : dim(series.color());
            }

            fill(guiGraphics, columnLeft, bottom - barHeight, columnLeft + width - gap, bottom, color, clipTop, clipBottom);
        }

        fill(guiGraphics, left, bottom, left + panelWidth, bottom + 1, AXIS_COLOR, clipTop, clipBottom);

        int labelTop = bottom + 1 + LABEL_SPACING;

        if (labelTop >= clipTop && labelTop + font.lineHeight <= clipBottom) {
            guiGraphics.text(font, series.min(), left, labelTop, -1);
            guiGraphics.text(font, series.max(), left + panelWidth - font.width(series.max()), labelTop, -1);
        }
    }

    private static int dim(int color) {
        int r = (color >> 16 & 0xFF) * DIM_PERCENT / 100;
        int g = (color >> 8 & 0xFF) * DIM_PERCENT / 100;
        int b = (color & 0xFF) * DIM_PERCENT / 100;

        return color & 0xFF000000 | r << 16 | g << 8 | b;
    }

    private static void fill(GuiGraphicsExtractor guiGraphics, int x1, int y1, int x2, int y2, int color, int clipTop, int clipBottom) {
        int top = Math.max(y1, clipTop);
        int bottom = Math.min(y2, clipBottom);

        if (top < bottom && x1 < x2) {
            guiGraphics.fill(x1, top, x2, bottom, color);
        }
    }

    private static int indentWidth(Font font, TooltipLine.Chart chart) {
        return font.width("  ".repeat(chart.indent()));
    }

    @NotNull
    private static ChartLayout layout(Font font, TooltipLine.Chart chart, int maxWidth) {
        List<Panel> panels = new ArrayList<>(chart.series().size());
        int x = 0;
        int row = 0;
        int width = 0;

        for (TooltipLine.Series series : chart.series()) {
            int panelWidth = panelWidth(font, chart, series);

            if (x > 0 && x + panelWidth > maxWidth) {
                x = 0;
                row++;
            }

            panels.add(new Panel(x, row, panelWidth));
            width = Math.max(width, x + panelWidth);
            x += panelWidth + PANEL_GAP;
        }

        return new ChartLayout(panels, row + 1, width);
    }

    private static int panelWidth(Font font, TooltipLine.Chart chart, TooltipLine.Series series) {
        int columns = series.last() - series.first() + 1;

        return Math.max(columns * chart.columnWidth(), font.width(series.min()) + LABEL_GAP + font.width(series.max()));
    }

    @NotNull
    private Layout getLayout(Font font) {
        int wrapWidth = Math.max(1, Minecraft.getInstance().getWindow().getGuiScaledWidth() - WINDOW_MARGIN);
        int capacity = getCapacity(font);

        if (layout == null || layout.wrapWidth() != wrapWidth || layout.capacity() != capacity) {
            Layout fitted = layout(font, wrapWidth, wrapWidth, capacity);

            if (fitted.totalLines() > capacity) {
                fitted = layout(font, wrapWidth, wrapWidth - SCROLLBAR_GAP - SCROLLBAR_WIDTH, capacity);
            }

            layout = fitted;
        }

        return layout;
    }

    @NotNull
    private Layout layout(Font font, int wrapWidth, int maxWidth, int capacity) {
        List<List<FormattedCharSequence>> text = new ArrayList<>(lines.size());
        List<ChartLayout> charts = new ArrayList<>(lines.size());
        int[] lineCounts = new int[lines.size()];
        int textWidth = 0;
        int total = 0;

        for (TooltipLine line : lines) {
            if (line instanceof TooltipLine.Text t) {
                List<FormattedCharSequence> rows = wrap(font, t, maxWidth);

                for (FormattedCharSequence row : rows) {
                    textWidth = Math.max(textWidth, font.width(row));
                }

                text.add(rows);
            } else {
                text.add(null);
            }
        }

        int width = textWidth;

        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i) instanceof TooltipLine.Chart c) {
                int indent = indentWidth(font, c);
                ChartLayout chart = layout(font, c, textWidth - indent);

                charts.add(chart);
                lineCounts[i] = chart.rows() * TooltipLine.Chart.PANEL_LINES;
                width = Math.max(width, indent + chart.width());
            } else {
                charts.add(null);
                lineCounts[i] = text.get(i).size();
            }

            total += lineCounts[i];
        }

        return new Layout(wrapWidth, capacity, text, charts, lineCounts, total, Math.min(total, capacity), width);
    }

    @NotNull
    private static List<FormattedCharSequence> wrap(Font font, TooltipLine.Text line, int maxWidth) {
        String indent = leadingSpaces(line.component().getString());
        int indentWidth = font.width(indent);
        List<FormattedCharSequence> rows = font.split(line.component(), Math.max(1, maxWidth - indentWidth));

        if (rows.isEmpty()) {
            return List.of(FormattedCharSequence.EMPTY);
        }

        if (rows.size() == 1 || indent.isEmpty()) {
            return rows;
        }

        List<FormattedCharSequence> indented = new ArrayList<>(rows.size());
        FormattedCharSequence prefix = FormattedCharSequence.forward(indent, Style.EMPTY);

        indented.add(rows.get(0));

        for (int i = 1; i < rows.size(); i++) {
            indented.add(FormattedCharSequence.composite(prefix, rows.get(i)));
        }

        return indented;
    }

    @NotNull
    private static String leadingSpaces(String text) {
        int i = 0;

        while (i < text.length() && text.charAt(i) == ' ') {
            i++;
        }

        return text.substring(0, i);
    }

    private int getCapacity(Font font) {
        int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();

        return Math.max(1, (screenHeight - SCREEN_MARGIN - getOtherComponentsHeight(font)) / LINE_HEIGHT);
    }

    private int getOtherComponentsHeight(Font font) {
        int height = 0;

        for (ClientTooltipComponent component : renderedComponents) {
            if (component != this) {
                height += component.getHeight(font);
            }
        }

        return renderedComponents.size() > 1 ? height + TITLE_GAP : height;
    }

    private record Panel(int x, int row, int width) {
    }

    private record ChartLayout(List<Panel> panels, int rows, int width) {
    }

    private record Layout(int wrapWidth, int capacity, List<List<FormattedCharSequence>> text, List<ChartLayout> charts, int[] lineCounts,
                          int totalLines, int visibleLines, int contentWidth) {
    }
}
