package com.yanny.ali.rei.compatibility.rei;

import com.yanny.ali.api.Rect;
import com.yanny.ali.compatibility.common.AbstractScrollWidget;
import com.yanny.ali.compatibility.common.GenericUtils;
import com.yanny.ali.configuration.LootCategory;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import oshi.util.tuples.Triplet;

import java.util.LinkedList;
import java.util.List;

public class ReiGameplayCategory extends ReiBaseCategory<ReiGameplayDisplay, ResourceLocation> {
    private final CategoryIdentifier<ReiGameplayDisplay> identifier;
    private final Component title;
    private final ItemStack icon;

    public ReiGameplayCategory(CategoryIdentifier<ReiGameplayDisplay> identifier, Component title, LootCategory<ResourceLocation> lootCategory) {
        super(lootCategory);
        this.identifier = identifier;
        this.title = title;
        this.icon = lootCategory.getIcon().getDefaultInstance();
    }

    @Override
    public List<Widget> setupDisplay(ReiGameplayDisplay display, Rectangle bounds) {
        Triplet<Component, Component, Rect> title = GenericUtils.prepareGameplayTitle(display.getId(), bounds.width - AbstractScrollWidget.getScrollbarExtraWidth());
        List<Widget> widgets = new LinkedList<>();
        Triplet<Rectangle, Rectangle, List<Widget>> prepared = prepareWidgets(display, bounds, 10);
        Rectangle innerBounds = prepared.getA();
        Rectangle fullBounds = prepared.getB();
        List<Widget> innerWidgets = new LinkedList<>(prepared.getC());

        fullBounds.move(bounds.getCenterX() - fullBounds.width / 2, bounds.y + PADDING);
        innerWidgets.add(Widgets.createLabel(new Point(0, 0), title.getA()).leftAligned().noShadow().color(0xFF000000).tooltip(title.getB()));
        widgets.add(Widgets.createCategoryBase(fullBounds));
        widgets.add(Widgets.withTranslate(
                new ReiScrollWidget(new Rect(0, 0, fullBounds.width - 2 * PADDING, fullBounds.height - 2 * PADDING), innerBounds.height, innerWidgets),
                fullBounds.x + PADDING,
                fullBounds.y + PADDING
        ));
        return widgets;
    }

    @Override
    public CategoryIdentifier<ReiGameplayDisplay> getCategoryIdentifier() {
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
