package com.yanny.ali.compatibility.rei;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.yanny.ali.api.*;
import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.plugin.GenericTooltipUtils;
import com.yanny.ali.plugin.widget.LootTableWidget;
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
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class ReiBaseCategory<T extends ReiBaseDisplay, U> implements DisplayCategory<T> {
    private final LootCategory<U> lootCategory;

    public ReiBaseCategory(LootCategory<U> lootCategory) {
        this.lootCategory = lootCategory;
    }

    @Override
    public abstract List<Widget> setupDisplay(T display, Rectangle bounds);

    @Override
    public int getDisplayWidth(T display) {
        return 9 * 18 + 8;
    }

    @Override
    public int getDisplayHeight() {
        return 8 * 18;
    }

    public LootCategory<U> getLootCategory() {
        return lootCategory;
    }

    protected List<Widget> getBaseWidget(T display, Rectangle bounds, int x, int y) {
        List<Widget> slotWidgets = new LinkedList<>();
        List<Widget> widgets = new LinkedList<>();
        ReiWidgetWrapper widget = new ReiWidgetWrapper(new LootTableWidget(getUtils(slotWidgets, bounds), display.getLootEntry(), x + 4, y + 4), bounds);

        widgets.add(Widgets.createTooltip(widget::getTooltip));
        widgets.add(widget);
        widgets.addAll(slotWidgets);
        return widgets;
    }

    @NotNull
    private IWidgetUtils getUtils(List<Widget> widgets, Rectangle bounds) {
        return new IWidgetUtils() {
            @Override
            public Pair<List<IEntryWidget>, Rect> createWidgets(IWidgetUtils registry, List<ILootEntry> entries, int x, int y, List<ILootFunction> functions, List<ILootCondition> conditions) {
                return PluginManager.CLIENT_REGISTRY.createWidgets(registry, entries, x, y, functions, conditions);
            }

            @Override
            public Rect getBounds(IClientUtils registry, List<ILootEntry> entries, int x, int y) {
                return PluginManager.CLIENT_REGISTRY.getBounds(registry, entries, x, y);
            }

            @Override
            public @Nullable WidgetDirection getWidgetDirection(ILootEntry entry) {
                return PluginManager.CLIENT_REGISTRY.getWidgetDirection(entry);
            }

            @Override
            public Rect addSlotWidget(Item item, ILootEntry entry, int x, int y, RangeValue chance, Optional<Pair<Holder<Enchantment>, Map<Integer, RangeValue>>> bonusChance, RangeValue count,
                                      Optional<Pair<Holder<Enchantment>, Map<Integer, RangeValue>>> bonusCount, List<ILootFunction> allFunctions, List<ILootCondition> allConditions) {
                EntryStack<ItemStack> stack = EntryStacks.of(item);

                stack.tooltip(GenericTooltipUtils.getTooltip(entry, chance, bonusChance, count, bonusCount, allFunctions, allConditions));
                widgets.add(Widgets.createSlot(new Point(x + bounds.getX() + 1, y + bounds.getY() + 1)).entry(stack).markOutput());
                widgets.add(Widgets.wrapRenderer(new Rectangle(x + bounds.getX(), y + bounds.getY(), 18, 18), new SlotCountRenderer(count)));
                return new Rect(x, y, 18, 18);
            }

            @Override
            public Rect addSlotWidget(TagKey<Item> item, ILootEntry entry, int x, int y, RangeValue chance, Optional<Pair<Holder<Enchantment>, Map<Integer, RangeValue>>> bonusChance, RangeValue count,
                                      Optional<Pair<Holder<Enchantment>, Map<Integer, RangeValue>>> bonusCount, List<ILootFunction> allFunctions, List<ILootCondition> allConditions) {
                EntryIngredient ingredient = EntryIngredients.ofItemTag(item);

                ingredient.map((stack) -> stack.tooltip(GenericTooltipUtils.getTooltip(entry, chance, bonusChance, count, bonusCount, allFunctions, allConditions)));
                widgets.add(Widgets.createSlot(new Point(x + bounds.getX() + 1, y + bounds.getY() + 1)).entries(ingredient).markOutput());
                widgets.add(Widgets.wrapRenderer(new Rectangle(x + bounds.getX(), y + bounds.getY(), 18, 18), new SlotCountRenderer(count)));
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
}
