package com.yanny.aci.compatibility;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.List;

public class ScrollableTooltip implements TooltipComponent, ClientTooltipComponent {
    private static final int LINE_HEIGHT = 10;
    private static final int SCREEN_MARGIN = 10;
    private static final int TITLE_GAP = 2;
    private static final int SCROLL_LINES = 3;
    private static final int SCROLL_STEP_DIVISOR = 8;
    private static final int SCROLLBAR_GAP = 3;
    private static final int SCROLLBAR_WIDTH = 1;
    private static final int TRACK_COLOR = 0x40FFFFFF;
    private static final int THUMB_COLOR = 0xFFA0A0A0;
    private static final long ACTIVE_MILLIS = 100;

    private static List<ClientTooltipComponent> renderedComponents = List.of();
    private static List<Component> activeLines = List.of();
    private static int offset = 0;
    private static int overflow = 0;
    private static int visibleLines = 0;
    private static long lastRender = 0;

    private final List<Component> lines;
    private final List<FormattedCharSequence> text;
    private final int textWidth;

    public ScrollableTooltip(List<Component> lines) {
        Font font = Minecraft.getInstance().font;

        this.lines = lines;
        this.text = lines.stream().map(Component::getVisualOrderText).toList();
        this.textWidth = text.stream().mapToInt(font::width).max().orElse(0);
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
    public int getHeight() {
        return getVisibleLines() * LINE_HEIGHT;
    }

    @Override
    public int getWidth(Font font) {
        return getVisibleLines() < text.size() ? textWidth + SCROLLBAR_GAP + SCROLLBAR_WIDTH : textWidth;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
        int visible = getVisibleLines();
        long now = Util.getMillis();

        if (now - lastRender > ACTIVE_MILLIS || !lines.equals(activeLines)) {
            activeLines = lines;
            offset = 0;
        }

        overflow = text.size() - visible;
        visibleLines = visible;
        offset = Math.min(offset, overflow);
        lastRender = now;

        for (int i = 0; i < visible; i++) {
            guiGraphics.drawString(font, text.get(offset + i), x, y + i * LINE_HEIGHT, -1);
        }

        if (overflow > 0) {
            int height = visible * LINE_HEIGHT;
            int trackX = x + textWidth + SCROLLBAR_GAP;
            int thumbHeight = Math.max(LINE_HEIGHT, height * visible / text.size());
            int thumbY = y + (height - thumbHeight) * offset / overflow;

            guiGraphics.fill(trackX, y, trackX + SCROLLBAR_WIDTH, y + height, TRACK_COLOR);
            guiGraphics.fill(trackX, thumbY, trackX + SCROLLBAR_WIDTH, thumbY + thumbHeight, THUMB_COLOR);
        }
    }

    private int getVisibleLines() {
        int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();

        return Math.min(text.size(), Math.max(1, (screenHeight - SCREEN_MARGIN - getOtherComponentsHeight()) / LINE_HEIGHT));
    }

    private int getOtherComponentsHeight() {
        int height = 0;

        for (ClientTooltipComponent component : renderedComponents) {
            if (component != this) {
                height += component.getHeight();
            }
        }

        return renderedComponents.size() > 1 ? height + TITLE_GAP : height;
    }
}
