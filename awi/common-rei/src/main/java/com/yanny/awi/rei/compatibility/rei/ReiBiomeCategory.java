package com.yanny.awi.rei.compatibility.rei;

import com.yanny.aci.api.Rect;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import oshi.util.tuples.Triplet;

import java.util.LinkedList;
import java.util.List;

public class ReiBiomeCategory extends ReiBaseCategory<ReiBiomeDisplay> {
    private final CategoryIdentifier<ReiBiomeDisplay> identifier;
    private final Component title;
    private final ItemStack icon;

    public ReiBiomeCategory(CategoryIdentifier<ReiBiomeDisplay> identifier, Component title) {
        this.identifier = identifier;
        this.title = title;
        this.icon = Items.GLOBE_BANNER_PATTERN.getDefaultInstance();
    }

    @Override
    public List<Widget> setupDisplay(ReiBiomeDisplay display, Rectangle bounds) {
        List<Widget> widgets = new LinkedList<>();
        Triplet<Rectangle, Rectangle, List<Widget>> prepared = prepareWidgets(display, bounds, 10);
        Rectangle innerBounds = prepared.getA();
        Rectangle fullBounds = prepared.getB();
        List<Widget> innerWidgets = new LinkedList<>(prepared.getC());
        Component title = Component.translatable("biome." + display.getEntry().id().getNamespace() + "." + display.getEntry().id().getPath());

        fullBounds.move(bounds.getCenterX() - fullBounds.width / 2, bounds.y + PADDING);
        innerWidgets.add(Widgets.createLabel(new Point(0, 0), title).leftAligned().noShadow().color(0));
        widgets.add(Widgets.createCategoryBase(fullBounds));
        widgets.add(Widgets.withTranslate(
                new ReiScrollWidget(new Rect(0, 0, fullBounds.width - 2 * PADDING, fullBounds.height - 2 * PADDING), innerBounds.height, innerWidgets),
                fullBounds.x + PADDING,
                fullBounds.y + PADDING
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
}
