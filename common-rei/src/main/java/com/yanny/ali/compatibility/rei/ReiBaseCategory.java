package com.yanny.ali.compatibility.rei;

import com.mojang.blaze3d.vertex.PoseStack;
import com.yanny.ali.api.IWidgetUtils;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.api.Rect;
import com.yanny.ali.plugin.client.ClientUtils;
import com.yanny.ali.plugin.client.EntryTooltipUtils;
import com.yanny.ali.plugin.client.TooltipUtils;
import com.yanny.ali.plugin.client.widget.LootTableWidget;
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
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
        List<Widget> slotWidgets = new LinkedList<>();
        List<Widget> widgets = new LinkedList<>();
        LootTableWidget widget = new LootTableWidget(getUtils(slotWidgets, bounds), display.getLootEntry(), x, y, CATEGORY_WIDTH);
        ReiWidgetWrapper widgetWrapper = new ReiWidgetWrapper(widget, bounds);

        widgets.add(Widgets.createTooltip(widgetWrapper::getTooltip));
        widgets.add(widgetWrapper);
        widgets.addAll(slotWidgets);
        return new WidgetHolder(widgets, widget.getRect());
    }

    @NotNull
    private IWidgetUtils getUtils(List<Widget> widgets, Rectangle bounds) {
        return new ClientUtils() {
            @Override
            public Rect addSlotWidget(Item item, LootPoolEntryContainer entry, int x, int y, Map<Enchantment, Map<Integer, RangeValue>> chance,
                                      Map<Enchantment, Map<Integer, RangeValue>> count, List<LootItemFunction> allFunctions, List<LootItemCondition> allConditions) {
                ItemStack itemStack = TooltipUtils.getItemStack(this, entry, item);
                EntryStack<ItemStack> stack = EntryStacks.of(itemStack);

                stack.tooltip(EntryTooltipUtils.getTooltip(this, entry, chance, count, allFunctions, allConditions));
                widgets.add(Widgets.createSlot(new Point(x + bounds.getX() + 1, y + bounds.getY() + 1)).entry(stack).markOutput());
                widgets.add(Widgets.wrapRenderer(new Rectangle(x + bounds.getX(), y + bounds.getY(), 18, 18), new SlotCountRenderer(count.get(null).get(0))));
                return new Rect(x, y, 18, 18);
            }

            @Override
            public Rect addSlotWidget(TagKey<Item> item, LootPoolEntryContainer entry, int x, int y, Map<Enchantment, Map<Integer, RangeValue>> chance,
                                      Map<Enchantment, Map<Integer, RangeValue>> count, List<LootItemFunction> allFunctions, List<LootItemCondition> allConditions) {
                EntryIngredient ingredient = EntryIngredients.ofItemTag(item);

                ingredient.map((stack) -> stack.tooltip(EntryTooltipUtils.getTooltip(this, entry, chance, count, allFunctions, allConditions)));
                widgets.add(Widgets.createSlot(new Point(x + bounds.getX() + 1, y + bounds.getY() + 1)).entries(ingredient).markOutput());
                widgets.add(Widgets.wrapRenderer(new Rectangle(x + bounds.getX(), y + bounds.getY(), 18, 18), new SlotCountRenderer(count.get(null).get(0))));
                return new Rect(x, y, 18, 18);
            }
        };
    }

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

    protected record WidgetHolder(List<Widget> widgets, Rect bounds){}
}
