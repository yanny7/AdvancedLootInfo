package com.yanny.ali.rei.compatibility.rei;

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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import oshi.util.tuples.Triplet;

import java.util.LinkedList;
import java.util.List;

public class ReiEntityCategory extends ReiBaseCategory<ReiEntityDisplay, EntityType<?>> {
    private static final int WIDGET_SIZE = 36;
    private static final int TEXT_OFFSET = 10;

    private final CategoryIdentifier<ReiEntityDisplay> identifier;
    private final Component title;
    private final ItemStack icon;

    public ReiEntityCategory(CategoryIdentifier<ReiEntityDisplay> identifier, Component title, LootCategory<EntityType<?>> lootCategory) {
        super(lootCategory);
        this.identifier = identifier;
        this.title = title;
        this.icon = lootCategory.getIcon().getDefaultInstance();
    }

    @Override
    public List<Widget> setupDisplay(ReiEntityDisplay display, Rectangle bounds) {
        List<Widget> widgets = new LinkedList<>();
        Rect rect = new Rect(0, 0, WIDGET_SIZE, WIDGET_SIZE);
        SpawnEggItem spawnEgg = SpawnEggItem.byId(display.getEntityType());
        Triplet<Rectangle, Rectangle, List<Widget>> prepared = prepareWidgets(display, bounds, TEXT_OFFSET + WIDGET_SIZE + PADDING);
        Rectangle innerBounds = prepared.getA();
        Rectangle fullBounds = prepared.getB();
        List<Widget> innerWidgets = new LinkedList<>(prepared.getC());

        if (spawnEgg != null) {
            innerWidgets.add(Widgets.createSlot(new Point(innerBounds.getX() + 1, innerBounds.getY() + TEXT_OFFSET + 1)).entry(EntryStacks.of(spawnEgg)).markInput());
        }

        innerWidgets.add(Widgets.wrapRenderer(new Rectangle(innerBounds.getCenterX() - WIDGET_SIZE / 2, TEXT_OFFSET, WIDGET_SIZE, WIDGET_SIZE), (graphics, bounds1, mouseX, mouseY, delta) -> {
            Level level = Minecraft.getInstance().level;

            if (level != null) {
                Entity entity = EntityStorage.getEntity(PluginManager.COMMON_REGISTRY, display.getEntityType(), level, display.getVariant());

                graphics.pose().pushMatrix();
                graphics.pose().translate(bounds1.getX(), bounds1.getY());
                GenericUtils.renderEntity(entity, rect, CATEGORY_WIDTH, graphics, mouseX + WIDGET_SIZE / 2 - innerBounds.width / 2, mouseY);
                graphics.pose().popMatrix();
            }
        }));
        innerWidgets.add(Widgets.createLabel(new Point(innerBounds.getCenterX(), 0), display.getEntityType().getDescription()).centered().noShadow().color(0xFF000000));
        fullBounds.move(bounds.getCenterX() - fullBounds.width / 2, bounds.y + PADDING);
        widgets.add(Widgets.createCategoryBase(fullBounds));
        widgets.add(Widgets.withTranslate(
                new ReiScrollWidget(new Rect(0, 0, fullBounds.width - 2 * PADDING, fullBounds.height - 2 * PADDING), innerBounds.height, innerWidgets),
                fullBounds.x + PADDING,
                fullBounds.y + PADDING
        ));
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
