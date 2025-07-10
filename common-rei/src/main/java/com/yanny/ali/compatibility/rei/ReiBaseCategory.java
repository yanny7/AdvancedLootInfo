package com.yanny.ali.compatibility.rei;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import com.yanny.ali.api.*;
import com.yanny.ali.plugin.client.ClientUtils;
import com.yanny.ali.plugin.client.widget.LootTableWidget;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.registries.LootCategory;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public abstract class ReiBaseCategory<T extends ReiBaseDisplay, U> implements DisplayCategory<T> {
    static final int CATEGORY_WIDTH = 9 * 18;
    static final int CATEGORY_HEIGHT = 8 * 18;
    static final int PADDING = 4;
    static final int ITEM_SIZE = 16;
    static final int SLOT_SIZE = 18;
    static final int OUT_SLOT_SIZE = 26;
    static final int SLOT_OFFSET = (SLOT_SIZE - ITEM_SIZE) / 2;
    static final int OUT_SLOT_OFFSET = (OUT_SLOT_SIZE - ITEM_SIZE) / 2;

    private final LootCategory<U> lootCategory;

    public ReiBaseCategory(LootCategory<U> lootCategory) {
        this.lootCategory = lootCategory;
    }

    @Override
    public abstract List<Widget> setupDisplay(T display, Rectangle bounds);

    @Override
    public int getDisplayWidth(T display) {
        return CATEGORY_WIDTH;
    }

    @Override
    public int getDisplayHeight() {
        return CATEGORY_HEIGHT;
    }

    public LootCategory<U> getLootCategory() {
        return lootCategory;
    }

    protected WidgetHolder getBaseWidget(T display, Rectangle bounds, int x, int y) {
        List<Holder> slotWidgets = new LinkedList<>();
        List<Widget> widgets = new LinkedList<>();
        RelativeRect rect = new RelativeRect(x, y, CATEGORY_WIDTH, 0);
        LootTableWidget widget = new LootTableWidget(getUtils(slotWidgets, bounds), display.getLootData(), rect, CATEGORY_WIDTH);
        ReiWidgetWrapper widgetWrapper = new ReiWidgetWrapper(widget, bounds);

        widgets.add(Widgets.createTooltip(widgetWrapper::getTooltip));
        widgets.add(widgetWrapper);
        slotWidgets.forEach((h) -> {
            Optional<ItemStack> left = h.item.left();
            Optional<TagKey<Item>> right = h.item.right();

            if (left.isPresent()) {
                ItemStack itemStack = left.get();
                EntryStack<ItemStack> stack = EntryStacks.of(itemStack);

                stack.tooltip(NodeUtils.toComponents(h.entry.getTooltip(), 0));
                widgets.add(Widgets.createSlot(new Point(h.rect.getX() + bounds.getX() + 1, h.rect.getY() + bounds.getY() + 1)).entry(stack).markOutput());
                widgets.add(Widgets.wrapRenderer(new Rectangle(h.rect.getX() + bounds.getX(), h.rect.getY() + bounds.getY(), 18, 18), new SlotCountRenderer(((IItemNode) h.entry).getCount()))); //FIXME move either inside
            } else if (right.isPresent()) {
                TagKey<Item> tagKey = right.get();
                EntryIngredient ingredient = EntryIngredients.ofItemTag(tagKey);

                ingredient.map((stack) -> stack.tooltip(NodeUtils.toComponents(h.entry.getTooltip(), 0)));
                widgets.add(Widgets.createSlot(new Point(h.rect.getX() + bounds.getX() + 1, h.rect.getY() + bounds.getY() + 1)).entries(ingredient).markOutput());
                widgets.add(Widgets.wrapRenderer(new Rectangle(h.rect.getX() + bounds.getX(), h.rect.getY() + bounds.getY(), 18, 18), new SlotCountRenderer(((IItemNode) h.entry).getCount())));
            }
        });
        return new WidgetHolder(widgets, widget.getRect());
    }

    @NotNull
    private IWidgetUtils getUtils(List<Holder> widgets, Rectangle bounds) {
        return new ClientUtils() {
            @Override
            public void addSlotWidget(Either<ItemStack, TagKey<Item>> item, IDataNode entry, RelativeRect rect) {
                widgets.add(new Holder(item, entry, rect));
            }
        };
    }

    private record Holder(Either<ItemStack, TagKey<Item>> item, IDataNode entry, RelativeRect rect) {}

    private static class SlotCountRenderer implements Renderer {
        @Nullable
        private Component count;
        private boolean isRange = false;

        public SlotCountRenderer(RangeValue count) {
            if (count.isRange() || count.min() > 1) {
                this.count = Component.literal(count.toIntString());
                isRange = count.isRange();
            }
        }

        @Override
        public void render(GuiGraphics guiGraphics, Rectangle bounds, int mouseX, int mouseY, float delta) {
            if (count != null) {
                Font font = Minecraft.getInstance().font;
                PoseStack stack = guiGraphics.pose();

                stack.pushPose();
                stack.translate(bounds.getX(), bounds.getY(), 0);

                if (isRange) {
                    stack.translate(17, 13, 200);
                    stack.pushPose();
                    stack.scale(0.5f, 0.5f, 0.5f);
                    //draw.fill(-font.width(count) - 2, -2, 2, 10, 255<<24 | 0);
                    guiGraphics.drawString(font, count, -font.width(count), 0, 16777215, false);
                    stack.popPose();
                } else {
                    stack.translate(18, 10, 200);
                    guiGraphics.drawString(font, count, -font.width(count), 0, 16777215, true);
                }

                stack.popPose();
            }
        }
    }

    protected record WidgetHolder(List<Widget> widgets, RelativeRect bounds){}
}
