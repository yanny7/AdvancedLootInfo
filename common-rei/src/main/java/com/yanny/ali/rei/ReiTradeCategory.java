package com.yanny.ali.rei;

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
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedList;
import java.util.List;

public class ReiTradeCategory extends ReiBaseCategory<ReiTradeDisplay, ResourceLocation> {
    private static final int OFFSET = 10;

    private final CategoryIdentifier<ReiTradeDisplay> identifier;
    private final Component title;
    private final ItemStack icon;

    public ReiTradeCategory(CategoryIdentifier<ReiTradeDisplay> identifier, Component title, LootCategory<ResourceLocation> lootCategory) {
        super(lootCategory);
        this.identifier = identifier;
        this.title = title;
        this.icon = lootCategory.getIcon().getDefaultInstance();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public List<Widget> setupDisplay(ReiTradeDisplay display, Rectangle bounds) {
        List<Widget> widgets = new LinkedList<>();
        String key = display.getId().equals("empty") ? "entity.minecraft.wandering_trader" : "entity.minecraft.villager." + display.getId();
        String id = display.getId().equals("empty") ? "wandering_trader" : display.getId();
        Component lootName = GenericUtils.ellipsis( key, id, bounds.width);
        Component fullText = Component.translatableWithFallback(key, id);
        int textWidth = Minecraft.getInstance().font.width(lootName);
        WidgetHolder holder = getBaseWidget(display, new Rectangle(0, 0, bounds.width, bounds.height), OFFSET);
        int with = Mth.clamp(holder.bounds().getWidth(), textWidth, bounds.width);
        int innerWidth = with % 2 == 0 ? with : with + 1; // made width even
        Rectangle innerBounds = new Rectangle(0, 0, innerWidth, holder.bounds().getHeight() + OFFSET);
        int height = Math.min(innerBounds.height + 2 * PADDING, bounds.height - 2 * PADDING);
        Rectangle fullBounds = new Rectangle(0, 0, innerBounds.width + 2 * PADDING, height);
        List<Widget> innerWidgets = new LinkedList<>(holder.widgets());

        fullBounds.move(bounds.getCenterX() - fullBounds.width / 2, bounds.y + PADDING);
        innerWidgets.add(Widgets.createLabel(new Point(innerBounds.getCenterX(), 0), lootName).noShadow().color(0).tooltip(fullText));
        widgets.add(Widgets.createCategoryBase(fullBounds));

        if (bounds.height >= innerBounds.height + 8) {
            innerBounds.move(bounds.getCenterX() - innerBounds.width / 2, bounds.y + 2 * PADDING);
            widgets.add(Widgets.withTranslate(Widgets.concat(innerWidgets), bounds.getCenterX() - Math.round(innerBounds.width / 2f), bounds.y + 2 * PADDING, 0));
        } else {
            Rectangle overflowBounds = new Rectangle(fullBounds.x + PADDING, fullBounds.y + PADDING, fullBounds.width - 2 * PADDING, fullBounds.height - 2 * PADDING);
            widgets.add(Widgets.overflowed(overflowBounds, Widgets.concatWithBounds(innerBounds, innerWidgets)));
        }

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
