package com.yanny.ali.plugin.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Divisor;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IWidget;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.plugin.client.widget.TextureWidget;
import it.unimi.dsi.fastutil.ints.IntIterator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.loot.entries.DynamicLoot;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class WidgetUtils {
    public static final ResourceLocation TEXTURE_LOC = Utils.modLoc("textures/gui/gui.png");
    public static final int VERTICAL_OFFSET = 2;
    public static final int GROUP_WIDGET_WIDTH = 7;
    public static final int GROUP_WIDGET_HEIGHT = 18;

    @NotNull
    public static IWidget getLootTableTypeWidget(int x, int y, int quality, float chance) {
        TextureWidget widget = new TextureWidget(TEXTURE_LOC, x, y, GROUP_WIDGET_WIDTH, GROUP_WIDGET_HEIGHT, 0, GROUP_WIDGET_HEIGHT);

        widget.tooltipText(EntryTooltipUtils.getLootTableTooltip(0, quality, chance));
        return widget;
    }

    @NotNull
    public static IWidget getLootPoolTypeWidget(int x, int y, RangeValue rolls, RangeValue bonusRolls) {
        TextureWidget widget = new TextureWidget(TEXTURE_LOC, x, y, GROUP_WIDGET_WIDTH, GROUP_WIDGET_HEIGHT, GROUP_WIDGET_WIDTH, GROUP_WIDGET_HEIGHT);

        widget.tooltipText(EntryTooltipUtils.getLootPoolTooltip(0, rolls, bonusRolls));
        return widget;
    }

    @NotNull
    public static IWidget getAlternativesWidget(int x, int y) {
        TextureWidget widget = new TextureWidget(TEXTURE_LOC, x, y, GROUP_WIDGET_WIDTH, GROUP_WIDGET_HEIGHT, 2 * GROUP_WIDGET_WIDTH, GROUP_WIDGET_HEIGHT);

        widget.tooltipText(EntryTooltipUtils.getAlternativesTooltip(0));
        return widget;
    }

    @NotNull
    public static IWidget getSequentialWidget(int x, int y) {
        TextureWidget widget = new TextureWidget(TEXTURE_LOC, x, y, GROUP_WIDGET_WIDTH, GROUP_WIDGET_HEIGHT, 3 * GROUP_WIDGET_WIDTH, GROUP_WIDGET_HEIGHT);

        widget.tooltipText(EntryTooltipUtils.getSequentialTooltip(0));
        return widget;
    }

    @NotNull
    public static IWidget getGroupWidget(int x, int y) {
        TextureWidget widget = new TextureWidget(TEXTURE_LOC, x, y, GROUP_WIDGET_WIDTH, GROUP_WIDGET_HEIGHT, 0, GROUP_WIDGET_HEIGHT);

        widget.tooltipText(EntryTooltipUtils.getGroupTooltip(0));
        return widget;
    }

    @NotNull
    public static IWidget getDynamicWidget(int x, int y, DynamicLoot entry, int sumWeight) {
        TextureWidget widget = new TextureWidget(TEXTURE_LOC, x, y, GROUP_WIDGET_WIDTH, GROUP_WIDGET_HEIGHT, 4 * GROUP_WIDGET_WIDTH, GROUP_WIDGET_HEIGHT);

        widget.tooltipText(EntryTooltipUtils.getDynamicTooltip(0, entry, sumWeight));
        return widget;
    }

    public static void blit(GuiGraphics guiGraphics, ResourceLocation pAtlasLocation, int pX, int pY, int pWidth, int pHeight, float pUOffset, float pVOffset, int pUWidth, int pVHeight) {
        guiGraphics.blit(RenderType::guiTextured, pAtlasLocation, pX, pY, pUOffset, pVOffset, pWidth, pHeight, pUWidth, pVHeight, 255, 255 );
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
