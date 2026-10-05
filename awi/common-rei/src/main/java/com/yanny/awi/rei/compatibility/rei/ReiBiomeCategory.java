package com.yanny.awi.rei.compatibility.rei;

import com.yanny.aci.api.Rect;
import com.yanny.aci.compatibility.ScrollableTooltip;
import com.yanny.aci.tooltip.TooltipLine;
import com.yanny.awi.compatibility.GenericUtils;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedList;
import java.util.List;

public class ReiBiomeCategory extends ReiBaseCategory<ReiBiomeDisplay> {
    private static final int TITLE_HEIGHT = 10;

    private final CategoryIdentifier<ReiBiomeDisplay> identifier;
    private final Component title;
    private final ItemStack icon;

    public ReiBiomeCategory(CategoryIdentifier<ReiBiomeDisplay> identifier, Component title, Item icon) {
        this.identifier = identifier;
        this.title = title;
        this.icon = icon.getDefaultInstance();
    }

    @Override
    public List<Widget> setupDisplay(ReiBiomeDisplay display, Rectangle bounds) {
        List<Widget> widgets = new LinkedList<>();
        List<GenericUtils.SpawnSlot> spawnSlots = GenericUtils.getSpawnSlots(display.getEntry().entry(), CATEGORY_WIDTH, TITLE_HEIGHT);
        PreparedWidgets prepared = prepareWidgets(display, bounds, TITLE_HEIGHT + GenericUtils.getSpawnSlotsHeight(display.getEntry().entry(), CATEGORY_WIDTH));
        Rectangle innerBounds = prepared.innerBounds();
        Rectangle fullBounds = prepared.fullBounds();
        List<Widget> innerWidgets = new LinkedList<>(prepared.widgets());
        Component title = GenericUtils.getBiomeTitle(display.getEntry().id());

        fullBounds.move(bounds.getCenterX() - fullBounds.width / 2, bounds.y + PADDING);
        innerWidgets.add(Widgets.createLabel(new Point(0, 0), title).leftAligned().noShadow().color(0));

        for (GenericUtils.SpawnSlot slot : spawnSlots) {
            if (slot.egg() != null) {
                EntryStack<ItemStack> stack = EntryStacks.of(slot.egg());

                stack.tooltipProcessor((s, tooltip) -> addConditions(tooltip, slot));
                innerWidgets.add(Widgets.createSlot(new Point(slot.x() + 1, slot.y() + 1)).entry(stack).markInput());
            } else {
                Rectangle slotBounds = new Rectangle(slot.x(), slot.y(), 18, 18);

                innerWidgets.add(Widgets.createSlotBase(slotBounds));
                innerWidgets.add(Widgets.wrapRenderer(slotBounds, (graphics, b, mouseX, mouseY, delta) -> GenericUtils.renderUnknownSpawnEgg(graphics, b.x, b.y)));
                innerWidgets.add(Widgets.createTooltip((point) -> slotBounds.contains(point) ? Tooltip.from(Tooltip.entry(new ScrollableTooltip(slot.getTooltip()))) : null));
            }
        }

        widgets.add(Widgets.createCategoryBase(fullBounds));
        widgets.add(Widgets.withTranslate(
                new ReiScrollWidget(new Rect(0, 0, fullBounds.width - 2 * PADDING, fullBounds.height - 2 * PADDING), prepared.contentWidth(), innerBounds.height, innerWidgets),
                fullBounds.x + PADDING,
                fullBounds.y + PADDING,
                0
        ));
        return widgets;
    }

    @Override
    public CategoryIdentifier<ReiBiomeDisplay> getCategoryIdentifier() {
        return identifier;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(icon);
    }

    private static Tooltip addConditions(Tooltip tooltip, GenericUtils.SpawnSlot slot) {
        List<TooltipLine> conditions = slot.getConditions();

        return conditions.isEmpty() ? tooltip : tooltip.add(new ScrollableTooltip(conditions));
    }
}
