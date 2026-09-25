package com.yanny.ali.rei.compatibility.rei;

import com.yanny.aci.api.Rect;
import com.yanny.ali.compatibility.common.GenericUtils;
import com.yanny.ali.compatibility.common.TraderHeader;
import com.yanny.ali.configuration.LootCategory;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Label;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ReiTradeCategory extends ReiBaseCategory<ReiTradeDisplay, Identifier> {
    private final CategoryIdentifier<ReiTradeDisplay> identifier;
    private final Component title;
    private final ItemStack icon;

    public ReiTradeCategory(CategoryIdentifier<ReiTradeDisplay> identifier, Component title, LootCategory<Identifier> lootCategory) {
        super(lootCategory);
        this.identifier = identifier;
        this.title = title;
        this.icon = lootCategory.getIcon().getDefaultInstance();
    }

    @Override
    public List<Widget> setupDisplay(ReiTradeDisplay display, Rectangle bounds) {
        TraderHeader header = GenericUtils.prepareTraderHeader(display.getType(), CATEGORY_WIDTH);
        List<Widget> widgets = new LinkedList<>();
        PreparedWidgets prepared = prepareWidgets(display, bounds, header.height(), header.minContentHeight());
        Rectangle innerBounds = prepared.innerBounds();
        Rectangle fullBounds = prepared.fullBounds();
        List<Widget> innerWidgets = new LinkedList<>(prepared.widgets());
        Rect titleRect = header.titleRect();
        Label title = Widgets.createLabel(new Point(titleRect.x(), titleRect.y()), header.title()).leftAligned().noShadow().color(0xFF000000);

        if (header.spawnEgg() != null) {
            innerWidgets.add(createSlot(header.spawnEggRect(), header.spawnEgg()));
        }

        if (header.titleTooltip() != null) {
            title.tooltip(header.titleTooltip());
        }

        addSlots(innerWidgets, display.getType().pois(), header.pois());

        if (!display.getType().accepts().isEmpty()) {
            Rect labelRect = header.acceptsLabelRect();

            innerWidgets.add(Widgets.createLabel(new Point(labelRect.x(), labelRect.y()), header.acceptsLabel()).leftAligned().noShadow().color(0xFF000000));
            addSlots(innerWidgets, display.getType().accepts(), header.accepts());
        }

        fullBounds.move(bounds.getCenterX() - fullBounds.width / 2, bounds.y + PADDING);
        innerWidgets.add(title);
        widgets.add(Widgets.createCategoryBase(fullBounds));
        widgets.add(Widgets.withTranslate(
                new ReiScrollWidget(new Rect(0, 0, fullBounds.width - 2 * PADDING, fullBounds.height - 2 * PADDING), prepared.contentWidth(), innerBounds.height, innerWidgets),
                fullBounds.x + PADDING,
                fullBounds.y + PADDING
        ));
        return widgets;
    }

    private static void addSlots(List<Widget> widgets, Set<? extends ItemLike> items, List<Rect> rects) {
        int i = 0;

        for (ItemLike item : items) {
            widgets.add(createSlot(rects.get(i++), item));
        }
    }

    @NotNull
    private static Widget createSlot(Rect rect, ItemLike item) {
        return Widgets.createSlot(new Point(rect.x() + SLOT_OFFSET, rect.y() + SLOT_OFFSET)).entry(EntryStacks.of(item)).markInput();
    }

    @Override
    public CategoryIdentifier<ReiTradeDisplay> getCategoryIdentifier() {
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
}
