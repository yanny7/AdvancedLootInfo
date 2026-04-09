package com.yanny.ali.rei.compatibility.rei;

import com.yanny.ali.api.Rect;
import com.yanny.ali.compatibility.common.GenericUtils;
import com.yanny.ali.configuration.LootCategory;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import oshi.util.tuples.Triplet;

import java.util.LinkedList;
import java.util.List;

public class ReiTradeCategory extends ReiBaseCategory<ReiTradeDisplay, ResourceLocation> {
    private final CategoryIdentifier<ReiTradeDisplay> identifier;
    private final Component title;
    private final ItemStack icon;

    public ReiTradeCategory(CategoryIdentifier<ReiTradeDisplay> identifier, Component title, LootCategory<ResourceLocation> lootCategory) {
        super(lootCategory);
        this.identifier = identifier;
        this.title = title;
        this.icon = lootCategory.getIcon().getDefaultInstance();
    }

    @Override
    public List<Widget> setupDisplay(ReiTradeDisplay display, Rectangle bounds) {
        Triplet<Component, Component, Rect> traderTitle = GenericUtils.prepareTraderTitle(display.getId(), bounds.width);
        List<Widget> widgets = new LinkedList<>();
        Triplet<Rectangle, Rectangle, List<Widget>> prepared = prepareWidgets(display, bounds, (display.getPois().isEmpty() ? 10 : 20) + (display.getAccepts().isEmpty() ? 0 : 20));
        Rectangle innerBounds = prepared.getA();
        Rectangle fullBounds = prepared.getB();
        List<Widget> innerWidgets = new LinkedList<>(prepared.getC());

        if (!display.getPois().isEmpty()) {
            int i = 1;

            for (Block block : display.getPois()) {
                innerWidgets.add(Widgets.createSlot(new Point(CATEGORY_WIDTH - i * 18, 1)).entry(EntryStacks.of(block)).markInput());
                i++;
            }
        }

        if (!display.getAccepts().isEmpty()) {
            int i = 0;
            int yOffset = (display.getPois().isEmpty() ? 10 : 20) + 1;
            Component t = Component.translatable("ali.util.advanced_loot_info.accepts");
            int width = Minecraft.getInstance().font.width(t);

            innerWidgets.add(Widgets.createLabel(new Point(0, yOffset + 5), t).leftAligned().noShadow().color(0xFF000000));

            for (Item item : display.getAccepts()) {
                innerWidgets.add(Widgets.createSlot(new Point(width + 2 + i * 18, yOffset)).entry(EntryStacks.of(item)).markInput());
                i++;
            }
        }

        fullBounds.move(bounds.getCenterX() - fullBounds.width / 2, bounds.y + PADDING);
        innerWidgets.add(Widgets.createLabel(new Point(0, 0), traderTitle.getA()).leftAligned().noShadow().color(0xFF000000).tooltip(traderTitle.getB()));
        widgets.add(Widgets.createCategoryBase(fullBounds));
        widgets.add(Widgets.withTranslate(
                new ReiScrollWidget(new Rect(0, 0, fullBounds.width - 2 * PADDING, fullBounds.height - 2 * PADDING), innerBounds.height, innerWidgets),
                fullBounds.x + PADDING,
                fullBounds.y + PADDING
        ));
        return widgets;
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
