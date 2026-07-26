package com.yanny.awi.plugin.client;

import com.yanny.aci.api.IWidget;
import com.yanny.aci.api.RelativeRect;
import com.yanny.awi.Utils;
import com.yanny.awi.api.IDataNode;
import com.yanny.awi.plugin.client.widget.TextureWidget;
import com.yanny.awi.plugin.common.nodes.PlacedFeatureNode;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class WidgetUtils {
    public static final ResourceLocation TEXTURE_LOC = Utils.modLoc("textures/gui/gui.png");

    private static final int GROUP_WIDGET_WIDTH = 7;
    private static final int GROUP_WIDGET_HEIGHT = 18;
    private static final int WIDGET_WIDTH = 18;
    private static final int WIDGET_HEIGHT = 18;

    @NotNull
    public static IWidget getAllWidget(RelativeRect rect, IDataNode node) {
        TextureWidget widget = new TextureWidget(TEXTURE_LOC, new RelativeRect(0, 0, GROUP_WIDGET_WIDTH, GROUP_WIDGET_HEIGHT, rect), 0, GROUP_WIDGET_HEIGHT);

        // show the placed feature's id as a white title at the top of its tooltip (like an item's name)
        if (node instanceof PlacedFeatureNode placedFeature && placedFeature.getFeatureId() != null) {
            widget.tooltipComponent(Component.literal(placedFeature.getFeatureId().toString()).withStyle(ChatFormatting.WHITE));
        }

        widget.tooltipText(node.getTooltip());
        return widget;
    }

    @NotNull
    public static IWidget getGenerationStepWidget(RelativeRect rect, IDataNode node, int step) {
        TextureWidget widget = new TextureWidget(TEXTURE_LOC, new RelativeRect(0, 0, WIDGET_WIDTH, WIDGET_HEIGHT, rect), WIDGET_WIDTH * step, 36);

        widget.tooltipText(node.getTooltip());
        return widget;
    }

    @NotNull
    public static IWidget getBaseTerrainWidget(RelativeRect rect, IDataNode node) {
        TextureWidget widget = new TextureWidget(TEXTURE_LOC, new RelativeRect(0, 0, WIDGET_WIDTH, WIDGET_HEIGHT, rect), 7, 18);

        widget.tooltipText(node.getTooltip());
        return widget;
    }
}
