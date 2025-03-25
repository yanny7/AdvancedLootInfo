package com.yanny.ali.plugin;

import com.mojang.math.Divisor;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IWidget;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.plugin.widget.TextureWidget;
import it.unimi.dsi.fastutil.ints.IntIterator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.ali.plugin.GenericTooltipUtils.translatable;
import static com.yanny.ali.plugin.GenericTooltipUtils.value;

public class WidgetUtils {
    public static final ResourceLocation TEXTURE_LOC = Utils.modLoc("textures/gui/gui.png");
    public static final int VERTICAL_OFFSET = 2;
    public static final int GROUP_WIDGET_WIDTH = 7;
    public static final int GROUP_WIDGET_HEIGHT = 18;

    @NotNull
    public static IWidget getLootTableTypeWidget(int x, int y) {
        TextureWidget widget = new TextureWidget(TEXTURE_LOC, x, y, GROUP_WIDGET_WIDTH, GROUP_WIDGET_HEIGHT, 0, GROUP_WIDGET_HEIGHT);
        List<Component> components = new LinkedList<>();

        components.add(translatable("ali.enum.group_type.all"));
        widget.tooltipText(components);
        return widget;
    }

    @NotNull
    public static IWidget getLootPoolTypeWidget(int x, int y, RangeValue rolls, RangeValue bonusRolls) {
        TextureWidget widget = new TextureWidget(TEXTURE_LOC, x, y, GROUP_WIDGET_WIDTH, GROUP_WIDGET_HEIGHT, GROUP_WIDGET_WIDTH, GROUP_WIDGET_HEIGHT);
        List<Component> components = new LinkedList<>();

        components.add(translatable("ali.enum.group_type.random"));
        components.add(getRolls(rolls, bonusRolls));
        widget.tooltipText(components);
        return widget;
    }

    @NotNull
    public static IWidget getAlternativesWidget(int x, int y) {
        TextureWidget widget = new TextureWidget(TEXTURE_LOC, x, y, GROUP_WIDGET_WIDTH, GROUP_WIDGET_HEIGHT, 2 * GROUP_WIDGET_WIDTH, GROUP_WIDGET_HEIGHT);
        List<Component> components = new LinkedList<>();

        components.add(translatable("ali.enum.group_type.alternatives"));
        widget.tooltipText(components);
        return widget;
    }

    @NotNull
    public static IWidget getSequentialWidget(int x, int y) {
        TextureWidget widget = new TextureWidget(TEXTURE_LOC, x, y, GROUP_WIDGET_WIDTH, GROUP_WIDGET_HEIGHT, 3 * GROUP_WIDGET_WIDTH, GROUP_WIDGET_HEIGHT);
        List<Component> components = new LinkedList<>();

        components.add(translatable("ali.enum.group_type.sequence"));
        widget.tooltipText(components);
        return widget;
    }

    @NotNull
    public static IWidget getGroupWidget(int x, int y) {
        TextureWidget widget = new TextureWidget(TEXTURE_LOC, x, y, GROUP_WIDGET_WIDTH, GROUP_WIDGET_HEIGHT, 0, GROUP_WIDGET_HEIGHT);
        List<Component> components = new LinkedList<>();

        components.add(translatable("ali.enum.group_type.all"));
        widget.tooltipText(components);
        return widget;
    }

    @NotNull
    public static IWidget getDynamicWidget(int x, int y) {
        TextureWidget widget = new TextureWidget(TEXTURE_LOC, x, y, GROUP_WIDGET_WIDTH, GROUP_WIDGET_HEIGHT, 4 * GROUP_WIDGET_WIDTH, GROUP_WIDGET_HEIGHT);
        List<Component> components = new LinkedList<>();

        components.add(translatable("ali.enum.group_type.dynamic"));
        widget.tooltipText(components);
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

    @NotNull
    private static Component getRolls(RangeValue rolls, RangeValue bonusRolls) {
        return translatable("ali.description.rolls", value(getTotalRolls(rolls, bonusRolls).toIntString(), "x"));
    }

    private static RangeValue getTotalRolls(RangeValue rolls, RangeValue bonusRolls) {
        if (bonusRolls.min() > 0 || bonusRolls.max() > 0) {
            return new RangeValue(bonusRolls).add(rolls);
        } else {
            return rolls;
        }
    }
}
