package com.yanny.ali.plugin.client;

import com.yanny.ali.Utils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IWidget;
import com.yanny.ali.api.RelativeRect;
import com.yanny.ali.api.WidgetDirection;
import com.yanny.ali.plugin.client.widget.TextureWidget;
import com.yanny.ali.plugin.common.NodeUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class WidgetUtils {
    public static final ResourceLocation TEXTURE_LOC = Utils.modLoc("textures/gui/gui.png");

    private static final int GROUP_WIDGET_WIDTH = 7;
    private static final int GROUP_WIDGET_HEIGHT = 18;

    @NotNull
    public static IWidget getAllWidget(RelativeRect rect, IDataNode node) {
        TextureWidget widget = new TextureWidget(TEXTURE_LOC, new RelativeRect(0, 0, GROUP_WIDGET_WIDTH, GROUP_WIDGET_HEIGHT, rect), 0, GROUP_WIDGET_HEIGHT);

        widget.tooltipText(node.getTooltip());
        return widget;
    }

    @NotNull
    public static IWidget getRandomWidget(RelativeRect rect, IDataNode node) {
        TextureWidget widget = new TextureWidget(TEXTURE_LOC, new RelativeRect(0, 0, GROUP_WIDGET_WIDTH, GROUP_WIDGET_HEIGHT, rect), GROUP_WIDGET_WIDTH, GROUP_WIDGET_HEIGHT);

        widget.tooltipText(node.getTooltip());
        return widget;
    }

    @NotNull
    public static IWidget getSequentialWidget(RelativeRect rect, IDataNode node) {
        TextureWidget widget = new TextureWidget(TEXTURE_LOC, new RelativeRect(0, 0, GROUP_WIDGET_WIDTH, GROUP_WIDGET_HEIGHT, rect), 2 * GROUP_WIDGET_WIDTH, GROUP_WIDGET_HEIGHT);

        widget.tooltipText(node.getTooltip());
        return widget;
    }

    @NotNull
    public static IWidget getAlternativesWidget(RelativeRect rect, IDataNode node) {
        TextureWidget widget = new TextureWidget(TEXTURE_LOC, new RelativeRect(0, 0, GROUP_WIDGET_WIDTH, GROUP_WIDGET_HEIGHT, rect), 3 * GROUP_WIDGET_WIDTH, GROUP_WIDGET_HEIGHT);

        widget.tooltipText(node.getTooltip());
        return widget;
    }

    @NotNull
    public static IWidget getDynamicWidget(RelativeRect rect, IDataNode node) {
        TextureWidget widget = new TextureWidget(TEXTURE_LOC, new RelativeRect(0, 0, GROUP_WIDGET_WIDTH, GROUP_WIDGET_HEIGHT, rect), 4 * GROUP_WIDGET_WIDTH, GROUP_WIDGET_HEIGHT);

        widget.tooltipText(node.getTooltip());
        return widget;
    }

    @NotNull
    public static IWidget getUnknownWidget(RelativeRect rect, IDataNode node) {
        return new TextureWidget(TEXTURE_LOC, new RelativeRect(0, 0, 18, 18, rect), 30, 0);
    }

    @NotNull
    public static IWidget getMissingWidget(RelativeRect rect) {
        return new TextureWidget(TEXTURE_LOC, new RelativeRect(0, 0, 18, 18, rect), 48, 0);
    }

    @NotNull
    public static IWidget getEmptyWidget(RelativeRect rect, IDataNode node) {
        TextureWidget widget = new TextureWidget(TEXTURE_LOC, new RelativeRect(0, 0, 18, 18, rect), 66, 0);

        widget.tooltipText(node.getTooltip());
        return widget;
    }

    @NotNull
    public static IWidget getArrowWidget(RelativeRect rect, IDataNode node) {
        TextureWidget widget = new TextureWidget(TEXTURE_LOC, new RelativeRect(0, 0, 24, 18, rect), 53, 18);

        widget.tooltipText(node.getTooltip());
        return widget;
    }

    @NotNull
    public static IWidget getLevelWidget(RelativeRect rect, IDataNode node, int level) {
        RelativeRect r = new RelativeRect(0, 0, 18, 18, rect);
        List<Component> components = NodeUtils.toComponents(node.getTooltip(), 0, Minecraft.getInstance().options.advancedItemTooltips);
        String txt = Integer.toString(level);
        int txtWidth = Minecraft.getInstance().font.width(txt);
        return new IWidget() {
            @Override
            public RelativeRect getRect() {
                return r;
            }

            @Override
            public WidgetDirection getDirection() {
                return WidgetDirection.VERTICAL;
            }

            @Override
            public List<Component> getTooltipComponents(int mouseX, int mouseY) {
                return components;
            }

            @Override
            public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
                guiGraphics.blitNineSliced(WidgetUtils.TEXTURE_LOC, r.getX(), r.getY(), r.width, r.height, 2, 2, 2, 2, 18, 18, 35, 18);
                guiGraphics.drawString(Minecraft.getInstance().font, txt, r.getX() + (r.width - txtWidth) / 2, r.getY() + 9, 0, false);
            }
        };
    }
}
