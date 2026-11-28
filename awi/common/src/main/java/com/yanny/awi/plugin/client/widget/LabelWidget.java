package com.yanny.awi.plugin.client.widget;

import com.yanny.aci.api.IWidget;
import com.yanny.aci.api.RelativeRect;
import com.yanny.aci.api.WidgetDirection;
import com.yanny.aci.tooltip.TooltipLine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LabelWidget implements IWidget {
    public static final int HEIGHT = 10;
    public static final int COLOR = 0xFF404040;

    private final RelativeRect rect;
    private final FormattedCharSequence text;
    private final int color;
    private final List<TooltipLine> tooltip;

    public LabelWidget(RelativeRect rect, Component text, int maxWidth, int color, List<TooltipLine> tooltip) {
        Font font = Minecraft.getInstance().font;

        this.rect = rect;
        this.color = color;
        this.tooltip = tooltip;

        this.text = fit(font, text, maxWidth).getVisualOrderText();

        rect.setDimensions(font.width(this.text), HEIGHT);
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
    public void render(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        guiGraphics.text(Minecraft.getInstance().font, text, rect.getX(), rect.getY() + 1, color, false);
    }

    @NotNull
    @Override
    public List<TooltipLine> getTooltipLines(int mouseX, int mouseY) {
        return tooltip;
    }

    @NotNull
    static Component fit(Font font, Component text, int maxWidth) {
        if (font.width(text) <= maxWidth) {
            return text;
        }

        String ellipsis = "…";
        return Component.literal(font.plainSubstrByWidth(text.getString(), maxWidth - font.width(ellipsis)) + ellipsis).withStyle(text.getStyle());
    }
}
