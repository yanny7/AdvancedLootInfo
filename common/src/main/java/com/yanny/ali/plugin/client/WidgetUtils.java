package com.yanny.ali.plugin.client;

import com.mojang.math.Divisor;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IWidget;
import com.yanny.ali.api.RelativeRect;
import com.yanny.ali.plugin.client.widget.TextureWidget;
import it.unimi.dsi.fastutil.ints.IntIterator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class WidgetUtils {
    public static final ResourceLocation TEXTURE_LOC = Utils.modLoc("textures/gui/gui.png");
    public static final int VERTICAL_OFFSET = 2;
    public static final int GROUP_WIDGET_WIDTH = 7;
    public static final int GROUP_WIDGET_HEIGHT = 18;

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

    public static void blitRepeating(GuiGraphics guiGraphics, ResourceLocation pAtlasLocation, int pTargetX, int pTargetY, int pTargetWidth, int pTargetHeight, int pSourceX, int pSourceY, int pSourceWidth, int pSourceHeight) {
        int i = pTargetX;
        int j;

        for(IntIterator intiterator = slices(pTargetWidth, pSourceWidth); intiterator.hasNext(); i += j) {
            j = intiterator.nextInt();
            int k = (pSourceWidth - j) / 2;
            int l = pTargetY;

            int i1;
            for(IntIterator intiterator1 = slices(pTargetHeight, pSourceHeight); intiterator1.hasNext(); l += i1) {
                i1 = intiterator1.nextInt();
                int j1 = (pSourceHeight - i1) / 2;
                guiGraphics.blit(pAtlasLocation, i, l, pSourceX + k, pSourceY + j1, j, i1);
            }
        }
    }

    public static void blitNineSliced(GuiGraphics guiGraphics, ResourceLocation pAtlasLocation, int pTargetX, int pTargetY, int pTargetWidth, int pTargetHeight, int pCornerWidth, int pCornerHeight, int pEdgeWidth, int pEdgeHeight, int pSourceWidth, int pSourceHeight, int pSourceX, int pSourceY) {
        pCornerWidth = Math.min(pCornerWidth, pTargetWidth / 2);
        pEdgeWidth = Math.min(pEdgeWidth, pTargetWidth / 2);
        pCornerHeight = Math.min(pCornerHeight, pTargetHeight / 2);
        pEdgeHeight = Math.min(pEdgeHeight, pTargetHeight / 2);

        if (pTargetWidth == pSourceWidth && pTargetHeight == pSourceHeight) {
            guiGraphics.blit(pAtlasLocation, pTargetX, pTargetY, pSourceX, pSourceY, pTargetWidth, pTargetHeight);
        } else if (pTargetHeight == pSourceHeight) {
            guiGraphics.blit(pAtlasLocation, pTargetX, pTargetY, pSourceX, pSourceY, pCornerWidth, pTargetHeight);
            blitRepeating(guiGraphics, pAtlasLocation, pTargetX + pCornerWidth, pTargetY, pTargetWidth - pEdgeWidth - pCornerWidth, pTargetHeight, pSourceX + pCornerWidth, pSourceY, pSourceWidth - pEdgeWidth - pCornerWidth, pSourceHeight);
            guiGraphics.blit(pAtlasLocation, pTargetX + pTargetWidth - pEdgeWidth, pTargetY, pSourceX + pSourceWidth - pEdgeWidth, pSourceY, pEdgeWidth, pTargetHeight);
        } else if (pTargetWidth == pSourceWidth) {
            guiGraphics.blit(pAtlasLocation, pTargetX, pTargetY, pSourceX, pSourceY, pTargetWidth, pCornerHeight);
            blitRepeating(guiGraphics, pAtlasLocation, pTargetX, pTargetY + pCornerHeight, pTargetWidth, pTargetHeight - pEdgeHeight - pCornerHeight, pSourceX, pSourceY + pCornerHeight, pSourceWidth, pSourceHeight - pEdgeHeight - pCornerHeight);
            guiGraphics.blit(pAtlasLocation, pTargetX, pTargetY + pTargetHeight - pEdgeHeight, pSourceX, pSourceY + pSourceHeight - pEdgeHeight, pTargetWidth, pEdgeHeight);
        } else {
            guiGraphics.blit(pAtlasLocation, pTargetX, pTargetY, pSourceX, pSourceY, pCornerWidth, pCornerHeight);
            blitRepeating(guiGraphics, pAtlasLocation, pTargetX + pCornerWidth, pTargetY, pTargetWidth - pEdgeWidth - pCornerWidth, pCornerHeight, pSourceX + pCornerWidth, pSourceY, pSourceWidth - pEdgeWidth - pCornerWidth, pCornerHeight);
            guiGraphics.blit(pAtlasLocation, pTargetX + pTargetWidth - pEdgeWidth, pTargetY, pSourceX + pSourceWidth - pEdgeWidth, pSourceY, pEdgeWidth, pCornerHeight);
            guiGraphics.blit(pAtlasLocation, pTargetX, pTargetY + pTargetHeight - pEdgeHeight, pSourceX, pSourceY + pSourceHeight - pEdgeHeight, pCornerWidth, pEdgeHeight);
            blitRepeating(guiGraphics, pAtlasLocation, pTargetX + pCornerWidth, pTargetY + pTargetHeight - pEdgeHeight, pTargetWidth - pEdgeWidth - pCornerWidth, pEdgeHeight, pSourceX + pCornerWidth, pSourceY + pSourceHeight - pEdgeHeight, pSourceWidth - pEdgeWidth - pCornerWidth, pEdgeHeight);
            guiGraphics.blit(pAtlasLocation, pTargetX + pTargetWidth - pEdgeWidth, pTargetY + pTargetHeight - pEdgeHeight, pSourceX + pSourceWidth - pEdgeWidth, pSourceY + pSourceHeight - pEdgeHeight, pEdgeWidth, pEdgeHeight);
            blitRepeating(guiGraphics, pAtlasLocation, pTargetX, pTargetY + pCornerHeight, pCornerWidth, pTargetHeight - pEdgeHeight - pCornerHeight, pSourceX, pSourceY + pCornerHeight, pCornerWidth, pSourceHeight - pEdgeHeight - pCornerHeight);
            blitRepeating(guiGraphics, pAtlasLocation, pTargetX + pCornerWidth, pTargetY + pCornerHeight, pTargetWidth - pEdgeWidth - pCornerWidth, pTargetHeight - pEdgeHeight - pCornerHeight, pSourceX + pCornerWidth, pSourceY + pCornerHeight, pSourceWidth - pEdgeWidth - pCornerWidth, pSourceHeight - pEdgeHeight - pCornerHeight);
            blitRepeating(guiGraphics, pAtlasLocation, pTargetX + pTargetWidth - pEdgeWidth, pTargetY + pCornerHeight, pCornerWidth, pTargetHeight - pEdgeHeight - pCornerHeight, pSourceX + pSourceWidth - pEdgeWidth, pSourceY + pCornerHeight, pEdgeWidth, pSourceHeight - pEdgeHeight - pCornerHeight);
        }
    }

    @NotNull
    private static IntIterator slices(int p_282197_, int p_282161_) {
        int i = Mth.positiveCeilDiv(p_282197_, p_282161_);
        return new Divisor(p_282197_, i);
    }
}
