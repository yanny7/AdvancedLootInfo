package com.yanny.ali.plugin.client.widget.trades;

import com.yanny.aci.api.IWidget;
import com.yanny.aci.api.RelativeRect;
import com.yanny.aci.api.WidgetDirection;
import com.yanny.aci.tooltip.TooltipLine;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IWidgetUtils;
import com.yanny.ali.plugin.client.WidgetUtils;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import net.minecraft.client.gui.GuiGraphics;
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
        ItemsToItemsNode node = (ItemsToItemsNode) entry;

        widgets = new ArrayList<>();

        addSlot(utils, rect, node.getSlotOptions(0), 0);
        addSlot(utils, rect, node.getSlotOptions(1), SECOND_COST_OFFSET);
        widgets.add(WidgetUtils.getArrowWidget(new RelativeRect(ARROW_OFFSET, 0, ARROW_WIDTH, SLOT_SIZE, rect), entry));
        addSlot(utils, rect, node.getSlotOptions(2), RESULT_OFFSET);

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
    public List<TooltipLine> getTooltipLines(int mouseX, int mouseY) {
        List<TooltipLine> components = new LinkedList<>();

        for (IWidget widget : widgets) {
            RelativeRect b = widget.getRect();

            if (b.contains(mouseX, mouseY)) {
                components.addAll(widget.getTooltipLines(mouseX, mouseY));
            }
        }

        return components;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        for (IWidget widget : widgets) {
            widget.render(guiGraphics, mouseX, mouseY);
        }
    }

    private static void addSlot(IWidgetUtils utils, RelativeRect rect, List<IDataNode> options, int offsetX) {
        if (!options.isEmpty()) {
            utils.addSlotWidget(options, new RelativeRect(offsetX, 0, SLOT_SIZE, SLOT_SIZE, rect));
        }
    }
}
