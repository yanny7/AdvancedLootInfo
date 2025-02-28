package com.yanny.ali.compatibility.rei;

import com.yanny.ali.api.Rect;
import com.yanny.ali.compatibility.common.GenericUtils;
import com.yanny.ali.registries.LootCategory;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;

import java.util.LinkedList;
import java.util.List;

public class ReiEntityCategory extends ReiBaseCategory<ReiEntityDisplay, Entity> {
    private final CategoryIdentifier<ReiEntityDisplay> identifier;
    private final Component title;
    private final ItemStack icon;

    public ReiEntityCategory(CategoryIdentifier<ReiEntityDisplay> identifier, Component title, LootCategory<Entity> lootCategory) {
        super(lootCategory);
        this.identifier = identifier;
        this.title = title;
        this.icon = lootCategory.getIcon();
    }

    @Override
    public List<Widget> setupDisplay(ReiEntityDisplay display, Rectangle bounds) {
        List<Widget> widgets = new LinkedList<>();
        int WIDGET_SIZE = 36;
        Rect rect = new Rect(0, 0, WIDGET_SIZE, WIDGET_SIZE);
        SpawnEggItem spawnEgg = SpawnEggItem.byId(display.getEntity().getType());
        Rectangle entityRect = new Rectangle(bounds.getCenterX() - 18, bounds.getY() + 14, 36, 48);

        widgets.add(Widgets.createCategoryBase(bounds));
        widgets.addAll(getBaseWidget(display, bounds, 0, 48));

        if (spawnEgg != null) {
            widgets.add(Widgets.createSlot(new Point(bounds.getX() + 4, bounds.getY() + 4))
                    .entry(EntryStacks.of(spawnEgg)).markInput());
        }

        widgets.add(Widgets.wrapRenderer(
                entityRect,
                (graphics, bounds1, mouseX, mouseY, delta) -> {
                    graphics.pose().pushPose();
                    graphics.pose().translate(bounds1.getX(), bounds1.getY(), 0);
                    GenericUtils.renderEntity(display.getEntity(), rect, 9 * 18, graphics, mouseX - bounds.getX(), mouseY - bounds.getY());
                    graphics.pose().popPose();
                }
        ));
        widgets.add(Widgets.wrapRenderer(
                new Rectangle(bounds.getCenterX() - Minecraft.getInstance().font.width(display.getEntity().getDisplayName()) / 2, bounds.getY() + 4, bounds.getWidth(), 8),
                (graphics, bounds1, mouseX, mouseY, delta) -> {
                    graphics.pose().pushPose();
                    graphics.pose().translate(bounds1.getX(), bounds1.getY(), 0);
                    graphics.drawString(Minecraft.getInstance().font, display.getEntity().getDisplayName(), 0, 0, 0, false);
                    graphics.pose().popPose();
                }
        ));
        widgets.add(Widgets.createTooltip(entityRect, display.getEntity().getDisplayName()));

        return widgets;
    }

    @Override
    public CategoryIdentifier<ReiEntityDisplay> getCategoryIdentifier() {
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
