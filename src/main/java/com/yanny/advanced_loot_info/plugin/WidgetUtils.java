package com.yanny.advanced_loot_info.plugin;

import com.yanny.advanced_loot_info.Utils;
import com.yanny.advanced_loot_info.api.RangeValue;
import com.yanny.advanced_loot_info.compatibility.EmiUtils;
import dev.emi.emi.api.widget.TextureWidget;
import dev.emi.emi.api.widget.Widget;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.advanced_loot_info.compatibility.EmiUtils.translatable;
import static com.yanny.advanced_loot_info.compatibility.EmiUtils.value;

public class WidgetUtils {
    public static final ResourceLocation TEXTURE_LOC = Utils.modLoc("textures/gui/gui.png");
    public static final int VERTICAL_OFFSET = 2;
    public static final int GROUP_WIDGET_WIDTH = 7;
    public static final int GROUP_WIDGET_HEIGHT = 18;

    @NotNull
    public static Widget getLootTableTypeWidget(int x, int y) {
        TextureWidget widget = new TextureWidget(TEXTURE_LOC, x, y, GROUP_WIDGET_WIDTH, GROUP_WIDGET_HEIGHT, 0, GROUP_WIDGET_HEIGHT);
        List<Component> components = new LinkedList<>();

        components.add(EmiUtils.translatable("emi.enum.group_type.all"));
        widget.tooltipText(components);
        return widget;
    }

    @NotNull
    public static Widget getLootPoolTypeWidget(int x, int y, RangeValue rolls, RangeValue bonusRolls) {
        TextureWidget widget = new TextureWidget(TEXTURE_LOC, x, y, GROUP_WIDGET_WIDTH, GROUP_WIDGET_HEIGHT, GROUP_WIDGET_WIDTH, GROUP_WIDGET_HEIGHT);
        List<Component> components = new LinkedList<>();

        components.add(EmiUtils.translatable("emi.enum.group_type.random"));
        components.add(getRolls(rolls, bonusRolls));
        widget.tooltipText(components);
        return widget;
    }

    @NotNull
    public static Widget getAlternativesWidget(int x, int y) {
        TextureWidget widget = new TextureWidget(TEXTURE_LOC, x, y, GROUP_WIDGET_WIDTH, 18, 2 * GROUP_WIDGET_WIDTH, 18);
        List<Component> components = new LinkedList<>();

        components.add(EmiUtils.translatable("emi.enum.group_type.alternatives"));
        widget.tooltipText(components);
        return widget;
    }

    @NotNull
    public static Widget getSequentialWidget(int x, int y) {
        TextureWidget widget = new TextureWidget(TEXTURE_LOC, x, y, GROUP_WIDGET_WIDTH, 18, 3 * GROUP_WIDGET_WIDTH, 18);
        List<Component> components = new LinkedList<>();

        components.add(EmiUtils.translatable("emi.enum.group_type.sequence"));
        widget.tooltipText(components);
        return widget;
    }

    @NotNull
    public static Widget getGroupWidget(int x, int y) {
        TextureWidget widget = new TextureWidget(TEXTURE_LOC, x, y, GROUP_WIDGET_WIDTH, GROUP_WIDGET_HEIGHT, 0, GROUP_WIDGET_HEIGHT);
        List<Component> components = new LinkedList<>();

        components.add(EmiUtils.translatable("emi.enum.group_type.all"));
        widget.tooltipText(components);
        return widget;
    }

    @NotNull
    private static Component getRolls(RangeValue rolls, RangeValue bonusRolls) {
        return translatable("emi.description.advanced_loot_info.rolls", value(getTotalRolls(rolls, bonusRolls).toIntString(), "x"));
    }

    private static RangeValue getTotalRolls(RangeValue rolls, RangeValue bonusRolls) {
        if (bonusRolls.min() > 0 || bonusRolls.max() > 0) {
            return new RangeValue(bonusRolls).add(rolls);
        } else {
            return rolls;
        }
    }
}
