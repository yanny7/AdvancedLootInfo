package com.yanny.ali.plugin;

import com.yanny.ali.Utils;
import com.yanny.ali.api.IWidget;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.plugin.widget.TextureWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.ali.plugin.TooltipUtils.translatable;
import static com.yanny.ali.plugin.TooltipUtils.value;

public class WidgetUtils {
    public static final ResourceLocation TEXTURE_LOC = Utils.modLoc("textures/gui/gui.png");
    public static final int VERTICAL_OFFSET = 2;
    public static final int GROUP_WIDGET_WIDTH = 7;
    public static final int GROUP_WIDGET_HEIGHT = 18;

    @NotNull
    public static IWidget getLootTableTypeWidget(int x, int y) {
        TextureWidget widget = new TextureWidget(TEXTURE_LOC, x, y, GROUP_WIDGET_WIDTH, GROUP_WIDGET_HEIGHT, 0, GROUP_WIDGET_HEIGHT);
        List<Component> components = new LinkedList<>();

        components.add(TooltipUtils.translatable("ali.enum.group_type.all"));
        widget.tooltipText(components);
        return widget;
    }

    @NotNull
    public static IWidget getLootPoolTypeWidget(int x, int y, RangeValue rolls, RangeValue bonusRolls) {
        TextureWidget widget = new TextureWidget(TEXTURE_LOC, x, y, GROUP_WIDGET_WIDTH, GROUP_WIDGET_HEIGHT, GROUP_WIDGET_WIDTH, GROUP_WIDGET_HEIGHT);
        List<Component> components = new LinkedList<>();

        components.add(TooltipUtils.translatable("ali.enum.group_type.random"));
        components.add(getRolls(rolls, bonusRolls));
        widget.tooltipText(components);
        return widget;
    }

    @NotNull
    public static IWidget getAlternativesWidget(int x, int y) {
        TextureWidget widget = new TextureWidget(TEXTURE_LOC, x, y, GROUP_WIDGET_WIDTH, GROUP_WIDGET_HEIGHT, 2 * GROUP_WIDGET_WIDTH, GROUP_WIDGET_HEIGHT);
        List<Component> components = new LinkedList<>();

        components.add(TooltipUtils.translatable("ali.enum.group_type.alternatives"));
        widget.tooltipText(components);
        return widget;
    }

    @NotNull
    public static IWidget getSequentialWidget(int x, int y) {
        TextureWidget widget = new TextureWidget(TEXTURE_LOC, x, y, GROUP_WIDGET_WIDTH, GROUP_WIDGET_HEIGHT, 3 * GROUP_WIDGET_WIDTH, GROUP_WIDGET_HEIGHT);
        List<Component> components = new LinkedList<>();

        components.add(TooltipUtils.translatable("ali.enum.group_type.sequence"));
        widget.tooltipText(components);
        return widget;
    }

    @NotNull
    public static IWidget getGroupWidget(int x, int y) {
        TextureWidget widget = new TextureWidget(TEXTURE_LOC, x, y, GROUP_WIDGET_WIDTH, GROUP_WIDGET_HEIGHT, 0, GROUP_WIDGET_HEIGHT);
        List<Component> components = new LinkedList<>();

        components.add(TooltipUtils.translatable("ali.enum.group_type.all"));
        widget.tooltipText(components);
        return widget;
    }

    @NotNull
    public static IWidget getDynamicWidget(int x, int y) {
        TextureWidget widget = new TextureWidget(TEXTURE_LOC, x, y, GROUP_WIDGET_WIDTH, GROUP_WIDGET_HEIGHT, 4 * GROUP_WIDGET_WIDTH, GROUP_WIDGET_HEIGHT);
        List<Component> components = new LinkedList<>();

        components.add(TooltipUtils.translatable("ali.enum.group_type.dynamic"));
        widget.tooltipText(components);
        return widget;
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
