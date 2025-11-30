package com.yanny.ali.compatibility.rei;

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

public class ReiGameplayCategory extends ReiBaseCategory<ReiGameplayDisplay, ResourceLocation> {
    private static final int OFFSET = 10;

    private final CategoryIdentifier<ReiGameplayDisplay> identifier;
    private final Component title;
    private final ItemStack icon;

    public ReiGameplayCategory(CategoryIdentifier<ReiGameplayDisplay> identifier, Component title, LootCategory<ResourceLocation> lootCategory) {
        super(lootCategory);
        this.identifier = identifier;
        this.title = title;
        this.icon = lootCategory.getIcon().getDefaultInstance();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public List<Widget> setupDisplay(ReiGameplayDisplay display, Rectangle bounds) {
        List<Widget> widgets = new LinkedList<>();
        String key = "ali/loot_table/" + display.getId().getPath();
        Component lootName = GenericUtils.ellipsis( key, display.getId().getPath(), bounds.width);
        Component fullText = Component.literal(display.getId().toString());
        int textWidth = Minecraft.getInstance().font.width(lootName);
        WidgetHolder holder = getBaseWidget(display, new Rectangle(0, 0, bounds.width, bounds.height), OFFSET);
        int with = Mth.clamp(holder.bounds().width, textWidth, bounds.width);
        int innerWidth = with % 2 == 0 ? with : with + 1; // made width even
        Rectangle innerBounds = new Rectangle(0, 0, innerWidth, holder.bounds().height + OFFSET);
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
