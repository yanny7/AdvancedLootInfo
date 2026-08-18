package com.yanny.ali.plugin.client.widget.trades;

import com.yanny.aci.api.IWidget;
import com.yanny.aci.api.RelativeRect;
import com.yanny.aci.api.WidgetDirection;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IWidgetUtils;
import com.yanny.ali.api.ListNode;
import com.yanny.ali.plugin.client.WidgetUtils;
import com.yanny.ali.plugin.client.widget.ItemWidget;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ItemListingWidget implements IWidget {
    private static final int SLOT_SIZE = 18;
    private static final int ARROW_WIDTH = 24;
    private static final int SECOND_COST_OFFSET = 20;
    private static final int ARROW_OFFSET = 40;
    private static final int RESULT_OFFSET = 66;
    private static final int WIDTH = RESULT_OFFSET + SLOT_SIZE;

    private final List<IWidget> widgets;
    private final RelativeRect bounds;

    public ItemListingWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth) {
        ListNode node = (ListNode) entry;

        widgets = new ArrayList<>();

        addWidget(utils, rect, node.nodes().get(0), 0, maxWidth);
        addWidget(utils, rect, node.nodes().get(1), SECOND_COST_OFFSET, maxWidth);
        widgets.add(WidgetUtils.getArrowWidget(new RelativeRect(ARROW_OFFSET, 0, ARROW_WIDTH, SLOT_SIZE, rect), entry));
        addWidget(utils, rect, node.nodes().get(2), RESULT_OFFSET, maxWidth);

        bounds = rect;
        bounds.setDimensions(WIDTH, SLOT_SIZE);
    }

    @NotNull
    @Override
    public RelativeRect getRect() {
        return bounds;
    }

    @NotNull
    @Override
    public WidgetDirection getDirection() {
        return WidgetDirection.VERTICAL;
    }

    @NotNull
    @Override
    public List<Component> getTooltipComponents(int mouseX, int mouseY) {
        List<Component> components = new LinkedList<>();

        for (IWidget widget : widgets) {
            RelativeRect b = widget.getRect();

            if (b.contains(mouseX, mouseY)) {
                components.addAll(widget.getTooltipComponents(mouseX, mouseY));
            }
        }

        return components;
    }

    @Override
    public void render(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        for (IWidget widget : widgets) {
            widget.render(guiGraphics, mouseX, mouseY);
        }
    }

    private void addWidget(IWidgetUtils utils, RelativeRect rect, IDataNode node, int offsetX, int maxWidth) {
        widgets.add(new ItemWidget(utils, node, new RelativeRect(offsetX, 0, SLOT_SIZE, SLOT_SIZE, rect), maxWidth));
    }
}
