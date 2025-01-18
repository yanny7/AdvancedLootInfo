package com.yanny.advanced_loot_info.compatibility;

import com.mojang.datafixers.util.Pair;
import com.yanny.advanced_loot_info.Utils;
import com.yanny.advanced_loot_info.network.GroupType;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.TextureWidget;
import dev.emi.emi.api.widget.Widget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class GroupWidget extends Widget {
    private final List<Widget> widgets;
    private final Bounds bounds;
    private final boolean hasGroups;
    private static final int MAX_IN_ROW = 9;
    private static final int HORIZONTAL_OFFSET = 5;
    private static final int VERTICAL_OFFSET = 1;
    private static final ResourceLocation TEXTURE_LOC = Utils.modLoc("textures/gui/gui.png");

    public GroupWidget(List<Widget> widgets, Bounds bounds, boolean hasGroups) {
        this.widgets = widgets;
        this.bounds = bounds;
        this.hasGroups = hasGroups;
    }

    @Override
    public Bounds getBounds() {
        return bounds;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        int lastY = 0;

        for (Widget widget : widgets) {
            widget.render(guiGraphics, mouseX, mouseY, delta);
            lastY = widget.getBounds().y();
        }

        if (hasGroups) {
            int top = bounds.y() + 18;
            int height = lastY - bounds.y() - 9;

            guiGraphics.blitRepeating(TEXTURE_LOC, bounds.x() + 2, top, 2, height, 0, 0, 2, 18);

            for (Widget widget : widgets) {
                if (!(widget instanceof LootSlotWidget) && widget.getBounds().y() > bounds.y() + 18) {
                    guiGraphics.blitRepeating(TEXTURE_LOC, bounds.x() + 3, widget.getBounds().y() + 8, 2, 2, 2, 0, 18, 2);
                }
            }
        }
    }

    @Override
    public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
        List<ClientTooltipComponent> components = new LinkedList<>();

        for (Widget widget : widgets) {
            Bounds b = widget.getBounds();

            if (b.contains(mouseX, mouseY)) {
                components.addAll(widget.getTooltip(mouseX, mouseY));
            }
        }

        return components;
    }

    @NotNull
    public static Pair<GroupWidget, Bounds> createWidget(EmiRecipe recipe, ItemGroup itemGroup, int posX, int posY) {
        List<Widget> widgets = new LinkedList<>();
        Bounds bounds = new Bounds(posX, posY, 0, 0);
        int x = posX, y = posY;

        if (itemGroup.items.size() > 1 || !itemGroup.groups.isEmpty()) {
            Widget widget = getGroupTypeWidget(itemGroup.type, x, y);
            posX = x = x + HORIZONTAL_OFFSET;
            bounds = extend(bounds, widget.getBounds());
            widgets.add(widget);
        }

        if (!itemGroup.items.isEmpty()) {
            for (ItemData item : itemGroup.items) {
                Widget widget = new LootSlotWidget(item, x, y).setCount(item.count).recipeContext(recipe);

                bounds = extend(bounds, widget.getBounds());
                widgets.add(widget);
                x += 18;

                if (x / (MAX_IN_ROW * 18) > 0) {
                    x = posX;
                    y += 18;
                }
            }

            x = posX;
            y = bounds.bottom() + VERTICAL_OFFSET;
        }

        for (ItemGroup group : itemGroup.groups) {
            Pair<GroupWidget, Bounds> w = createWidget(recipe, group, x, y);

            bounds = extend(bounds, w.getSecond());
            y = bounds.bottom() + VERTICAL_OFFSET;

            widgets.add(w.getFirst());
        }

        return new Pair<>(new GroupWidget(widgets,bounds, !itemGroup.groups.isEmpty()), bounds);
    }

    @NotNull
    private static TextureWidget getGroupTypeWidget(GroupType type, int x, int y) {
        TextureWidget widget = switch (type) {
            case ALL -> new TextureWidget(TEXTURE_LOC, x, y, 5, 18, 0, 18);
            case RANDOM -> new TextureWidget(TEXTURE_LOC, x, y, 5, 18, 5, 18);
            case ALTERNATIVES -> new TextureWidget(TEXTURE_LOC, x, y, 5, 18, 10, 18);
            case SEQUENCE -> new TextureWidget(TEXTURE_LOC, x, y, 5, 18, 15, 18);
        };

        widget.tooltipText(List.of(EmiUtils.translatableType("emi.enum.group_type", type)));
        return widget;
    }

    @NotNull
    private static Bounds extend(Bounds b1, Bounds b2) {
        return new Bounds(
                Math.min(b1.x(), b2.x()),
                Math.min(b1.y(), b2.y()),
                Math.max(b2.right() - b1.left(), b1.width()),
                Math.max(b2.bottom() - b1.top(), b1.height())
        );
    }
}
