package com.yanny.ali.plugin;

import com.yanny.ali.Utils;
import com.yanny.ali.api.IWidget;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.plugin.widget.TextureWidget;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.entries.DynamicLoot;
import org.jetbrains.annotations.NotNull;

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
}
