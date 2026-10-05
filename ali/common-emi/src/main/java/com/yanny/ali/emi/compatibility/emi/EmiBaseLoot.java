package com.yanny.ali.emi.compatibility.emi;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.IWidget;
import com.yanny.aci.api.Rect;
import com.yanny.aci.api.RelativeRect;
import com.yanny.aci.compatibility.AbstractScrollWidget;
import com.yanny.aci.tooltip.TooltipNodePalette;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IItemNode;
import com.yanny.ali.api.IWidgetUtils;
import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.plugin.client.ClientUtils;
import com.yanny.ali.plugin.client.TooltipUtils;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.config.SidebarSide;
import dev.emi.emi.screen.RecipeScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class EmiBaseLoot extends BasicEmiRecipe {
    static final int CATEGORY_WIDTH = 9 * 18 - AbstractScrollWidget.getScrollbarExtraWidth();
    private final IDataNode lootTable;
    private final int widgetX;
    private final int widgetY;
    private int itemsWidth = -1;
    private int itemsHeight;

    public EmiBaseLoot(EmiRecipeCategory category, ResourceLocation id, IDataNode lootTable, int widgetX, int widgetY, List<ItemStack> inputs, List<ItemStack> outputs) {
        // '/' prefix marks the recipe as synthetic - EMI requires it for recipes that are not present in the recipe manager
        // the height passed to super is never read - getDisplayHeight() below overrides BasicEmiRecipe's accessor
        super(category, new ResourceLocation(id.getNamespace(), "/" + id.getPath()), CATEGORY_WIDTH + AbstractScrollWidget.getScrollbarExtraWidth(), 0);
        this.lootTable = lootTable;
        this.widgetX = widgetX;
        this.widgetY = widgetY;
        this.inputs.addAll(inputs.stream().map(EmiStack::of).toList());
        this.outputs.addAll(outputs.stream().map(EmiStack::of).toList());
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        Rect rect = new Rect(0, 0, CATEGORY_WIDTH + AbstractScrollWidget.getScrollbarExtraWidth(), Math.min(getDisplayHeight(), widgetHolder.getHeight()));
        Layout layout = layout();
        List<Widget> widgets = new ArrayList<>();

        widgets.addAll(layout.slots().stream().map((h) -> {
            EmiIngredient ingredient = h.item.map(EmiStack::of, EmiIngredient::of);
            IItemNode node = (IItemNode) h.entry;
            EmiLootSlotWidget widget = new EmiLootSlotWidget(h.entry, ingredient, h.rect.getX(), h.rect.getY(), TooltipUtils.getSlotCount(node), node.hasPredicates());

            widget.recipeContext(h.recipe);
            return (Widget) widget;
        }).toList());
        widgets.addAll(getAdditionalWidgets(widgetHolder));
        widgets.add(layout.widget());
        widgetHolder.add(new EmiScrollWidget(rect, getItemsWidth(), getContentHeight(), widgets));
    }

    /** EMI clamps this only for the recipe background - the fill/tree/screenshot buttons are positioned from the raw value. */
    @Override
    public final int getDisplayHeight() {
        return Math.min(getContentHeight(), getAvailableHeight());
    }

    private int getContentHeight() {
        return Math.max(getHeaderHeight() + getItemsHeight(), getMinContentHeight());
    }

    /** Mirrors {@code RecipeScreen#init} + {@code RecipeTab#getVerticalRecipeSpace} - EMI exposes neither. */
    private int getAvailableHeight() {
        int screenHeight = Math.min(EmiConfig.maximumRecipeScreenHeight, Minecraft.getInstance().getWindow().getGuiScaledHeight() - 52 - EmiConfig.verticalMargin);
        int height = screenHeight - 46;

        if (EmiConfig.workstationLocation == SidebarSide.BOTTOM
                && (!EmiApi.getRecipeManager().getWorkstations(getCategory()).isEmpty() || RecipeScreen.resolve != null)) {
            height -= 23;
        }

        return height;
    }

    @Override
    public Recipe<?> getBackingRecipe() {
        return null;
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    protected int getItemsHeight() {
        measure();
        return itemsHeight;
    }

    protected int getItemsWidth() {
        measure();
        return itemsWidth;
    }

    protected int getMinContentHeight() {
        return 0;
    }

    protected List<Widget> getAdditionalWidgets(WidgetHolder widgetHolder) {
        return List.of();
    }

    /** Vertical space this category needs above the item tree - must match the {@code widgetY} passed to the constructor. */
    protected abstract int getHeaderHeight();

    abstract IWidget getRootWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth);

    private void measure() {
        if (itemsWidth < 0) {
            Bounds bounds = layout().widget().getBounds();

            itemsWidth = bounds.width();
            itemsHeight = bounds.height();
        }
    }

    @NotNull
    private Layout layout() {
        List<Holder> slots = new ArrayList<>();
        RelativeRect rect = new RelativeRect(widgetX, widgetY, CATEGORY_WIDTH, 0);
        Widget widget = new EmiWidgetWrapper(getRootWidget(getEmiUtils(this, slots), lootTable, rect, CATEGORY_WIDTH));

        return new Layout(widget, slots);
    }

    @NotNull
    private IWidgetUtils getEmiUtils(EmiRecipe recipe, List<Holder> slotWidgets) {
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
                slotWidgets.add(new Holder(this, item, entry, rect, recipe));
            }
        };
    }

    private record Layout(Widget widget, List<Holder> slots) {}

    private record Holder(IWidgetUtils utils, Either<ItemStack, TagKey<? extends ItemLike>> item, IDataNode entry, RelativeRect rect, EmiRecipe recipe) {}
}
