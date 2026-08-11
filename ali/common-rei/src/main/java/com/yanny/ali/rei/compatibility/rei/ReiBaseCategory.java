package com.yanny.ali.rei.compatibility.rei;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.api.RelativeRect;
import com.yanny.aci.tooltip.CoreTooltipUtils;
import com.yanny.aci.tooltip.TooltipNodePalette;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IItemNode;
import com.yanny.ali.api.IWidgetUtils;
import com.yanny.ali.compatibility.common.AbstractScrollWidget;
import com.yanny.ali.configuration.LootCategory;
import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.plugin.client.ClientUtils;
import com.yanny.ali.plugin.client.WidgetUtils;
import com.yanny.ali.plugin.client.widget.LootTableWidget;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.config.SearchFieldLocation;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;
import oshi.util.tuples.Triplet;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public abstract class ReiBaseCategory<T extends ReiBaseDisplay, U> implements DisplayCategory<T> {
    static final int CATEGORY_WIDTH = 9 * 18;
    static final int PADDING = 4;
    static final int ITEM_SIZE = 16;
    static final int SLOT_SIZE = 18;
    static final int OUT_SLOT_SIZE = 26;
    static final int SLOT_OFFSET = (SLOT_SIZE - ITEM_SIZE) / 2;
    static final int OUT_SLOT_OFFSET = (OUT_SLOT_SIZE - ITEM_SIZE) / 2;

    // Mirrors REI's own recipe-window layout math (RoughlyEnoughItems 12.1.785, DefaultDisplayViewingScreen#init):
    // the window is INNER_PADDING_Y + (getDisplayHeight() + DISPLAY_GAP) * displaysPerPage tall, capped by the space
    // between the tab bar and the (optionally centered) search field and by the user's "Max recipes page height".
    // Re-verify these numbers against REI's sources when porting to another branch.
    private static final int INNER_PADDING_Y = 36;
    private static final int DISPLAY_GAP = 4;
    private static final int OUTER_PADDING = 2;
    private static final int TAB_SIZE = 28;
    private static final int COMPACT_TAB_SIZE = 24;
    private static final int TAB_OVERFLOW_HEIGHT = 16;
    private static final int CENTERED_SEARCH_HEIGHT = 22;
    private static final int MIN_PAGE_HEIGHT = 100;

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

    /**
     * One loot table per page - the tree wants the whole window, and it makes the window/display relation
     * deterministic so {@link #getDisplayHeight()} does not have to guess how many displays REI will stack.
     */
    @Override
    public int getFixedDisplaysPerPage() {
        return 1;
    }

    /**
     * REI asks for the height per <i>category</i>, not per display, so we report the largest height REI can
     * actually give us instead of a fixed guess. Recomputed on every screen init (including resizes); smaller loot
     * tables are clamped down again by {@link #prepareWidgets}, they just get a taller, partly empty viewport.
     * The tab-overflow allowance is always included because whether the tabs overflow is not exposed by REI's API -
     * under-reporting the top margin would make the display draw over the window frame.
     */
    @Override
    public int getDisplayHeight() {
        ConfigObject config = ConfigObject.getInstance();
        int topMargin = OUTER_PADDING + (config.isUsingCompactTabs() ? COMPACT_TAB_SIZE : TAB_SIZE) - 2 + TAB_OVERFLOW_HEIGHT;
        int bottomMargin = OUTER_PADDING + (config.getSearchFieldLocation() == SearchFieldLocation.CENTER ? CENTERED_SEARCH_HEIGHT : 0);
        int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int largestHeight = Math.min(Math.max(screenHeight - topMargin - bottomMargin, MIN_PAGE_HEIGHT), config.getMaxRecipesPageHeight());

        return largestHeight - INNER_PADDING_Y - DISPLAY_GAP;
    }

    public LootCategory<U> getLootCategory() {
        return lootCategory;
    }

    protected Triplet<Rectangle, Rectangle, List<Widget>> prepareWidgets(T display, Rectangle bounds, int offset) {
        WidgetHolder holder = getBaseWidget(display, new Rectangle(0, 0, bounds.width, bounds.height), offset);
        Rectangle innerBounds = new Rectangle(0, 0, bounds.width, holder.bounds().getHeight() + offset);
        int height = Math.min(innerBounds.height + 2 * PADDING, bounds.height - 2 * PADDING);
        Rectangle fullBounds = new Rectangle(0, 0, innerBounds.width + 3 * PADDING + AbstractScrollWidget.getScrollbarExtraWidth(), height);

        return new Triplet<>(innerBounds, fullBounds, holder.widgets);
    }

    protected WidgetHolder getBaseWidget(T display, Rectangle bounds, int y) {
        List<Holder> slotWidgets = new LinkedList<>();
        List<Widget> widgets = new LinkedList<>();
        RelativeRect rect = new RelativeRect(0, y, CATEGORY_WIDTH, 0);
        LootTableWidget widget = new LootTableWidget(getUtils(slotWidgets), display.getLootData(), rect, CATEGORY_WIDTH);
        ReiWidgetWrapper widgetWrapper = new ReiWidgetWrapper(widget);

        widgets.add(Widgets.createTooltip(widgetWrapper::getTooltip));
        widgets.add(widgetWrapper);
        slotWidgets.forEach((h) -> {
            Optional<ItemStack> left = h.item.left();
            Optional<TagKey<? extends ItemLike>> right = h.item.right();
            IItemNode node = (IItemNode) h.entry;
            Point point = new Point(h.rect.getX() + bounds.getX() + 1, h.rect.getY() + bounds.getY() + 1);
            Rectangle slotBounds = new Rectangle(h.rect.getX() + bounds.getX(), h.rect.getY() + bounds.getY(), SLOT_SIZE, SLOT_SIZE);
            Slot slot = null;

            if (left.isPresent()) {
                ItemStack itemStack = left.get();
                EntryStack<ItemStack> stack = EntryStacks.of(itemStack);

                stack.tooltip((s) -> CoreTooltipUtils.toComponents(h.entry.getTooltip(), 0, Minecraft.getInstance().options.advancedItemTooltips));
                slot = Widgets.createSlot(point).entry(stack).markOutput();
            } else if (right.isPresent()) {
                TagKey<? extends ItemLike> tagKey = right.get();
                EntryIngredient ingredient = EntryIngredients.ofItemTag(tagKey);

                ingredient.map((stack) -> stack.tooltip((s) -> CoreTooltipUtils.toComponents(h.entry.getTooltip(), 0, Minecraft.getInstance().options.advancedItemTooltips)));
                slot = Widgets.createSlot(point).entries(ingredient).markOutput();
            }

            if (slot != null) {
                if (node.hasPredicates()) {
                    // the slot draws its own background right before the item, so draw base + tint ourselves to get the tint in between
                    slot.disableBackground();
                    widgets.add(Widgets.createSlotBackground(point));
                    widgets.add(Widgets.wrapRenderer(slotBounds, new PredicatesRenderer()));
                }

                widgets.add(slot);
            }

            widgets.add(Widgets.wrapRenderer(slotBounds, new SlotCountRenderer(node.getCount())));
        });
        return new WidgetHolder(widgets, widget.getRect());
    }

    @NotNull
    private IWidgetUtils getUtils(List<Holder> widgets) {
        return new ClientUtils() {
            @Nullable
            @Override
            public String getTranslationKey(int index) {
                return null;
            }

            @NotNull
            @Override
            public TooltipNodePalette getTooltipCache() {
                return PluginManager.getInstance().clientRegistry.getTooltipCache();
            }

            @Override
            public void addSlotWidget(Either<ItemStack, TagKey<? extends ItemLike>> item, IDataNode entry, RelativeRect rect) {
                widgets.add(new Holder(item, entry, rect));
            }
        };
    }

    private record Holder(Either<ItemStack, TagKey<? extends ItemLike>> item, IDataNode entry, RelativeRect rect) {}

    private static class PredicatesRenderer implements Renderer {
        @Override
        public void render(GuiGraphics guiGraphics, Rectangle bounds, int mouseX, int mouseY, float delta) {
            guiGraphics.fill(bounds.getX() + 1, bounds.getY() + 1, bounds.getMaxX() - 1, bounds.getMaxY() - 1, WidgetUtils.PREDICATES_COLOR);
        }
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
                Matrix3x2fStack stack = guiGraphics.pose();


                stack.pushMatrix();
                stack.translate(bounds.getX(), bounds.getY());

                if (isRange) {
                    stack.translate(17, 13);
                    stack.pushMatrix();
                    stack.scale(0.5f);
                    //draw.fill(-font.width(count) - 2, -2, 2, 10, 255<<24 | 0);
                    guiGraphics.drawString(font, count, -font.width(count), 0, -1, false);
                    stack.popMatrix();
                } else {
                    stack.translate(18, 10);
                    guiGraphics.drawString(font, count, -font.width(count), 0, -1, true);
                }

                stack.popMatrix();
            }
        }
    }

    protected record WidgetHolder(List<Widget> widgets, RelativeRect bounds){}
}
