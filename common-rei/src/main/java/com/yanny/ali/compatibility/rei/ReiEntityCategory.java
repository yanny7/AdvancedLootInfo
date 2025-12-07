package com.yanny.ali.compatibility.rei;

import com.yanny.ali.api.Rect;
import com.yanny.ali.compatibility.common.EntityStorage;
import com.yanny.ali.compatibility.common.GenericUtils;
import com.yanny.ali.configuration.LootCategory;
import com.yanny.ali.manager.PluginManager;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;

import java.util.LinkedList;
import java.util.List;

public class ReiEntityCategory extends ReiBaseCategory<ReiEntityDisplay, EntityType<?>> {
    private static final int WIDGET_SIZE = 36;
    private static final int TEXT_OFFSET = 10;
    private static final int OFFSET = TEXT_OFFSET + WIDGET_SIZE + PADDING;

    private final CategoryIdentifier<ReiEntityDisplay> identifier;
    private final Component title;
    private final ItemStack icon;

    public ReiEntityCategory(CategoryIdentifier<ReiEntityDisplay> identifier, Component title, LootCategory<EntityType<?>> lootCategory) {
        super(lootCategory);
        this.identifier = identifier;
        this.title = title;
        this.icon = lootCategory.getIcon().getDefaultInstance();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public List<Widget> setupDisplay(ReiEntityDisplay display, Rectangle bounds) {
        List<Widget> widgets = new LinkedList<>();
        Rect rect = new Rect(0, 0, WIDGET_SIZE, WIDGET_SIZE);
        Component entityLabel = display.getEntityType().getDescription();
        SpawnEggItem spawnEgg = SpawnEggItem.byId(display.getEntityType());
        int textWidth = Minecraft.getInstance().font.width(entityLabel);
        WidgetHolder holder = getBaseWidget(display, new Rectangle(0, 0, bounds.width, bounds.height), OFFSET);
        int with = Mth.clamp(Math.max(holder.bounds().getWidth(), textWidth), WIDGET_SIZE + (SLOT_SIZE + PADDING) * 2, bounds.width);
        int innerWidth = with % 2 == 0 ? with : with + 1; // made width even
        Rectangle innerBounds = new Rectangle(0, 0, innerWidth, holder.bounds().getHeight() + OFFSET);
        int height = Math.min(innerBounds.height + 2 * PADDING, bounds.height - 2 * PADDING);
        Rectangle fullBounds = new Rectangle(0, 0, innerBounds.width + 2 * PADDING, height);
        List<Widget> innerWidgets = new LinkedList<>(holder.widgets());


        if (spawnEgg != null) {
            innerWidgets.add(Widgets.createSlot(new Point(innerBounds.getX() + 1, innerBounds.getY() + TEXT_OFFSET + 1)).entry(EntryStacks.of(spawnEgg)).markInput());
        }

        innerWidgets.add(Widgets.wrapRenderer(new Rectangle(innerBounds.getCenterX() - WIDGET_SIZE / 2, TEXT_OFFSET, WIDGET_SIZE, WIDGET_SIZE), (graphics, bounds1, mouseX, mouseY, delta) -> {
            Level level = Minecraft.getInstance().level;

            if (level != null) {
                Entity entity = EntityStorage.getEntity(PluginManager.COMMON_REGISTRY, display.getEntityType(), level, display.getVariant());

                graphics.pose().pushPose();
                graphics.pose().translate(bounds1.getX(), bounds1.getY(), 0);
                GenericUtils.renderEntity(entity, rect, CATEGORY_WIDTH, graphics, mouseX + innerBounds.width / 2, mouseY);
                graphics.pose().popPose();
            }
        }));
        innerWidgets.add(Widgets.createLabel(new Point(innerBounds.getCenterX(), 0), display.getEntityType().getDescription()));
        fullBounds.move(bounds.getCenterX() - fullBounds.width / 2, bounds.y + PADDING);
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
