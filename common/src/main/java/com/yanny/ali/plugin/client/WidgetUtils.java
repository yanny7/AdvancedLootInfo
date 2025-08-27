package com.yanny.ali.plugin.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Divisor;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IWidget;
import com.yanny.ali.api.RelativeRect;
import com.yanny.ali.api.WidgetDirection;
import com.yanny.ali.plugin.client.widget.TextureWidget;
import com.yanny.ali.plugin.common.NodeUtils;
import it.unimi.dsi.fastutil.ints.IntIterator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

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
                blitNineSliced(guiGraphics, WidgetUtils.TEXTURE_LOC, r.getX(), r.getY(), r.width, r.height, 2, 2, 2, 2, 18, 18, 35, 18);
                guiGraphics.drawString(Minecraft.getInstance().font, txt, r.getX() + (r.width - txtWidth) / 2, r.getY() + 9, 0xFF000000, false);
            }
        };
    }

    public static void blit(GuiGraphics guiGraphics, ResourceLocation pAtlasLocation, int pX, int pY, int pWidth, int pHeight, float pUOffset, float pVOffset, int pUWidth, int pVHeight) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, pAtlasLocation, pX, pY, pUOffset, pVOffset, pWidth, pHeight, pUWidth, pVHeight, 255, 255 );
    }

    public static void blit(GuiGraphics guiGraphics, ResourceLocation pAtlasLocation, int pX, int pY, float pUOffset, float pVOffset, int pWidth, int pHeight) {
        blit(guiGraphics, pAtlasLocation, pX, pY, pWidth, pHeight, pUOffset, pVOffset, pWidth, pHeight);
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
                blit(guiGraphics, pAtlasLocation, i, l, pSourceX + k, pSourceY + j1, j, i1);
            }
        }
    }

    public static void blitNineSliced(GuiGraphics guiGraphics, ResourceLocation pAtlasLocation, int pTargetX, int pTargetY, int pTargetWidth, int pTargetHeight, int pCornerWidth, int pCornerHeight, int pEdgeWidth, int pEdgeHeight, int pSourceWidth, int pSourceHeight, int pSourceX, int pSourceY) {
        pCornerWidth = Math.min(pCornerWidth, pTargetWidth / 2);
        pEdgeWidth = Math.min(pEdgeWidth, pTargetWidth / 2);
        pCornerHeight = Math.min(pCornerHeight, pTargetHeight / 2);
        pEdgeHeight = Math.min(pEdgeHeight, pTargetHeight / 2);

        if (pTargetWidth == pSourceWidth && pTargetHeight == pSourceHeight) {
            blit(guiGraphics, pAtlasLocation, pTargetX, pTargetY, pSourceX, pSourceY, pTargetWidth, pTargetHeight);
        } else if (pTargetHeight == pSourceHeight) {
            blit(guiGraphics, pAtlasLocation, pTargetX, pTargetY, pSourceX, pSourceY, pCornerWidth, pTargetHeight);
            blitRepeating(guiGraphics, pAtlasLocation, pTargetX + pCornerWidth, pTargetY, pTargetWidth - pEdgeWidth - pCornerWidth, pTargetHeight, pSourceX + pCornerWidth, pSourceY, pSourceWidth - pEdgeWidth - pCornerWidth, pSourceHeight);
            blit(guiGraphics, pAtlasLocation, pTargetX + pTargetWidth - pEdgeWidth, pTargetY, pSourceX + pSourceWidth - pEdgeWidth, pSourceY, pEdgeWidth, pTargetHeight);
        } else if (pTargetWidth == pSourceWidth) {
            blit(guiGraphics, pAtlasLocation, pTargetX, pTargetY, pSourceX, pSourceY, pTargetWidth, pCornerHeight);
            blitRepeating(guiGraphics, pAtlasLocation, pTargetX, pTargetY + pCornerHeight, pTargetWidth, pTargetHeight - pEdgeHeight - pCornerHeight, pSourceX, pSourceY + pCornerHeight, pSourceWidth, pSourceHeight - pEdgeHeight - pCornerHeight);
            blit(guiGraphics, pAtlasLocation, pTargetX, pTargetY + pTargetHeight - pEdgeHeight, pSourceX, pSourceY + pSourceHeight - pEdgeHeight, pTargetWidth, pEdgeHeight);
        } else {
            blit(guiGraphics, pAtlasLocation, pTargetX, pTargetY, pSourceX, pSourceY, pCornerWidth, pCornerHeight);
            blitRepeating(guiGraphics, pAtlasLocation, pTargetX + pCornerWidth, pTargetY, pTargetWidth - pEdgeWidth - pCornerWidth, pCornerHeight, pSourceX + pCornerWidth, pSourceY, pSourceWidth - pEdgeWidth - pCornerWidth, pCornerHeight);
            blit(guiGraphics, pAtlasLocation, pTargetX + pTargetWidth - pEdgeWidth, pTargetY, pSourceX + pSourceWidth - pEdgeWidth, pSourceY, pEdgeWidth, pCornerHeight);
            blit(guiGraphics, pAtlasLocation, pTargetX, pTargetY + pTargetHeight - pEdgeHeight, pSourceX, pSourceY + pSourceHeight - pEdgeHeight, pCornerWidth, pEdgeHeight);
            blitRepeating(guiGraphics, pAtlasLocation, pTargetX + pCornerWidth, pTargetY + pTargetHeight - pEdgeHeight, pTargetWidth - pEdgeWidth - pCornerWidth, pEdgeHeight, pSourceX + pCornerWidth, pSourceY + pSourceHeight - pEdgeHeight, pSourceWidth - pEdgeWidth - pCornerWidth, pEdgeHeight);
            blit(guiGraphics, pAtlasLocation, pTargetX + pTargetWidth - pEdgeWidth, pTargetY + pTargetHeight - pEdgeHeight, pSourceX + pSourceWidth - pEdgeWidth, pSourceY + pSourceHeight - pEdgeHeight, pEdgeWidth, pEdgeHeight);
            blitRepeating(guiGraphics, pAtlasLocation, pTargetX, pTargetY + pCornerHeight, pCornerWidth, pTargetHeight - pEdgeHeight - pCornerHeight, pSourceX, pSourceY + pCornerHeight, pCornerWidth, pSourceHeight - pEdgeHeight - pCornerHeight);
            blitRepeating(guiGraphics, pAtlasLocation, pTargetX + pCornerWidth, pTargetY + pCornerHeight, pTargetWidth - pEdgeWidth - pCornerWidth, pTargetHeight - pEdgeHeight - pCornerHeight, pSourceX + pCornerWidth, pSourceY + pCornerHeight, pSourceWidth - pEdgeWidth - pCornerWidth, pSourceHeight - pEdgeHeight - pCornerHeight);
            blitRepeating(guiGraphics, pAtlasLocation, pTargetX + pTargetWidth - pEdgeWidth, pTargetY + pCornerHeight, pCornerWidth, pTargetHeight - pEdgeHeight - pCornerHeight, pSourceX + pSourceWidth - pEdgeWidth, pSourceY + pCornerHeight, pEdgeWidth, pSourceHeight - pEdgeHeight - pCornerHeight);
        }
    }

    public static void innerBlit(VertexConsumer consumer, Matrix4f matrix, int xStart, int xEnd, int yStart, int yEnd, int z, int width, int height, float u, float v, int texWidth, int texHeight) {
        innerBlit(consumer, matrix, xStart, xEnd, yStart, yEnd, z, u / (float)texWidth, (u + (float)width) / (float)texWidth, v / (float)texHeight, (v + (float)height) / (float)texHeight);
    }

    private static void innerBlit(VertexConsumer consumer, Matrix4f matrix, int xStart, int xEnd, int yStart, int yEnd, int z, float uStart, float uEnd, float vStart, float vEnd) {
        consumer.addVertex(matrix, (float)xStart, (float)yEnd, (float)z).setUv(uStart, vEnd).setColor(-1);
        consumer.addVertex(matrix, (float)xEnd, (float)yEnd, (float)z).setUv(uEnd, vEnd).setColor(-1);
        consumer.addVertex(matrix, (float)xEnd, (float)yStart, (float)z).setUv(uEnd, vStart).setColor(-1);
        consumer.addVertex(matrix, (float)xStart, (float)yStart, (float)z).setUv(uStart, vStart).setColor(-1);
    }

    @NotNull
    private static IntIterator slices(int p_282197_, int p_282161_) {
        int i = Mth.positiveCeilDiv(p_282197_, p_282161_);
        return new Divisor(p_282197_, i);
    }
}
