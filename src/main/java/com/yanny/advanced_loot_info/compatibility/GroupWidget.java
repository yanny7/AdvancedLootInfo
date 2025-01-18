package com.yanny.advanced_loot_info.compatibility;

import com.mojang.datafixers.util.Pair;
import com.yanny.advanced_loot_info.Utils;
import com.yanny.advanced_loot_info.network.RangeValue;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.TextureWidget;
import dev.emi.emi.api.widget.Widget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.advanced_loot_info.compatibility.EmiUtils.translatable;
import static com.yanny.advanced_loot_info.compatibility.EmiUtils.value;

public class GroupWidget extends Widget {
    private final List<Widget> widgets;
    private final Bounds bounds;
    private final boolean hasGroups;
    private static final int MAX_IN_ROW = 9;
    private static final int VERTICAL_OFFSET = 2;
    private static final int GROUP_WIDGET_WIDTH = 7;
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

            guiGraphics.blitRepeating(TEXTURE_LOC, bounds.x() + 3, top, 2, height, 0, 0, 2, 18);

            for (Widget widget : widgets) {
                if (!(widget instanceof LootSlotWidget) && widget.getBounds().y() > bounds.y() + 18) {
                    guiGraphics.blitRepeating(TEXTURE_LOC, bounds.x() + 4, widget.getBounds().y() + 8, 3, 2, 2, 0, 18, 2);
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

        if (itemGroup.items.size() > 1 || !itemGroup.groups.isEmpty() || itemGroup.rollsHolder != null) {
            Widget widget = getGroupTypeWidget(itemGroup, x, y);
            posX = x = x + GROUP_WIDGET_WIDTH;
            bounds = extend(bounds, widget.getBounds());
            widgets.add(widget);
        }

        if (!itemGroup.items.isEmpty()) {
            for (ItemData item : itemGroup.items) {
                Widget widget = new LootSlotWidget(item, x, y).setCount(item.count).recipeContext(recipe);

                bounds = extend(bounds, widget.getBounds());
                widgets.add(widget);
                x += 18;

                if (x / ((MAX_IN_ROW - 1) * 18) > 0) {
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
    private static TextureWidget getGroupTypeWidget(ItemGroup group, int x, int y) {
        List<Component> components = new LinkedList<>();
        TextureWidget widget = switch (group.type) {
            case ALL -> new TextureWidget(TEXTURE_LOC, x, y, GROUP_WIDGET_WIDTH, 18, 0, 18);
            case RANDOM -> new TextureWidget(TEXTURE_LOC, x, y, GROUP_WIDGET_WIDTH, 18, GROUP_WIDGET_WIDTH, 18);
            case ALTERNATIVES -> new TextureWidget(TEXTURE_LOC, x, y, GROUP_WIDGET_WIDTH, 18, 2 * GROUP_WIDGET_WIDTH, 18);
            case SEQUENCE -> new TextureWidget(TEXTURE_LOC, x, y, GROUP_WIDGET_WIDTH, 18, 3 * GROUP_WIDGET_WIDTH, 18);
        };

        components.add(EmiUtils.translatableType("emi.enum.group_type", group.type));

        if (group.rollsHolder != null) {
            components.add(getRolls(group.rollsHolder));
        }

        widget.tooltipText(components);
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

    @NotNull
    private static Component getRolls(ItemGroup.RollsHolder holder) {
        return translatable("emi.description.advanced_loot_info.rolls", value(getRolls(holder.rolls(), holder.bonusRolls()).toIntString(), "x"));
    }

    private static RangeValue getRolls(RangeValue rolls, RangeValue bonusRolls) {
        if (bonusRolls.min() > 0 || bonusRolls.max() > 0) {
            return new RangeValue(bonusRolls).add(rolls);
        } else {
            return rolls;
        }
    }
}
