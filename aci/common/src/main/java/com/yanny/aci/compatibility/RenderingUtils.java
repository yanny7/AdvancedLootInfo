package com.yanny.aci.compatibility;

import com.mojang.math.Divisor;
import it.unimi.dsi.fastutil.ints.IntIterator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class RenderingUtils {
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
