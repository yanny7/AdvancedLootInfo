package com.yanny.awi.emi.compatibility.emi;

import com.yanny.aci.api.IWidget;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.api.Rect;
import com.yanny.aci.api.RelativeRect;
import com.yanny.aci.tooltip.TooltipNodePalette;
import com.yanny.awi.api.IDataNode;
import com.yanny.awi.api.IWidgetUtils;
import com.yanny.awi.compatibility.AbstractScrollWidget;
import com.yanny.awi.compatibility.GenericUtils;
import com.yanny.awi.manager.PluginManager;
import com.yanny.awi.plugin.client.ClientUtils;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public abstract class EmiBaseLoot extends BasicEmiRecipe {
    static final int CATEGORY_WIDTH = 9 * 18 - AbstractScrollWidget.getScrollbarExtraWidth();
    private final Widget widget;
    private final List<Holder> slotWidgets = new LinkedList<>();

    public EmiBaseLoot(EmiRecipeCategory category, ResourceLocation id, IDataNode lootTable, int widgetX, int widgetY, List<ItemStack> inputs, List<Block> outputs) {
        // '/' prefix marks the recipe as synthetic - EMI requires it for recipes that are not present in the recipe manager
        // the height passed to super is never read - getDisplayHeight() below overrides BasicEmiRecipe's accessor
        super(category, new ResourceLocation(id.getNamespace(), "/" + id.getPath()), CATEGORY_WIDTH + AbstractScrollWidget.getScrollbarExtraWidth(), 0);
        RelativeRect rect = new RelativeRect(widgetX, widgetY, CATEGORY_WIDTH, 0);
        widget = new EmiWidgetWrapper(getRootWidget(getEmiUtils(this), lootTable, rect, CATEGORY_WIDTH));
        this.inputs.addAll(inputs.stream().map(EmiStack::of).toList());
        this.outputs.addAll(outputs.stream().map(EmiStack::of).toList());
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        Rect rect = new Rect(0, 0, CATEGORY_WIDTH + AbstractScrollWidget.getScrollbarExtraWidth(), Math.min(getDisplayHeight(), widgetHolder.getHeight()));
        List<Widget> widgets = new ArrayList<>();

        widgets.addAll(slotWidgets.stream().map((h) -> {
            // Blocks without an item form (fire, end_gateway, *_plant, ...) are drawn as a 3D block model.
            if (GenericUtils.rendersAsBlockModel(h.block)) {
                EmiBlockSlotWidget blockWidget = new EmiBlockSlotWidget(h.entry, h.block, h.rect.getX(), h.rect.getY());

                blockWidget.recipeContext(h.recipe);
                return blockWidget;
            }

            EmiIngredient ingredient;

            if (GenericUtils.rendersAsFluid(h.block)) {
                ingredient = EmiStack.of(h.block.defaultBlockState().getFluidState().getType());
            } else {
                ingredient = EmiStack.of(h.block);
            }

            EmiLootSlotWidget widget = new EmiLootSlotWidget(h.entry, ingredient, h.rect.getX(), h.rect.getY(), new RangeValue(1));

            widget.recipeContext(h.recipe);
            return (Widget) widget;
        }).toList());
        widgets.addAll(getAdditionalWidgets(widgetHolder));
        widgets.add(widget);
        widgetHolder.add(new EmiScrollWidget(rect, getDisplayHeight(), widgets));
    }

    /**
     * EMI treats this as the <i>maximum</i> height the recipe wants and clamps it to the space it can actually
     * provide ({@link WidgetHolder#getHeight()}), so reporting the full content height is safe - a tall biome
     * simply gets a page of its own.
     */
    @Override
    public final int getDisplayHeight() {
        return getHeaderHeight() + getItemsHeight();
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
        return widget.getBounds().height();
    }

    protected List<Widget> getAdditionalWidgets(WidgetHolder widgetHolder) {
        return List.of();
    }

    /** Vertical space this category needs above the item tree - must match the {@code widgetY} passed to the constructor. */
    protected abstract int getHeaderHeight();

    abstract IWidget getRootWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth);

    @NotNull
    private IWidgetUtils getEmiUtils(EmiRecipe recipe) {
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
            public void addSlotWidget(Block block, IDataNode entry, RelativeRect rect) {
                slotWidgets.add(new Holder(this, block, entry, rect, recipe));
            }
        };
    }

    private record Holder(IWidgetUtils utils, Block block, IDataNode entry, RelativeRect rect, EmiRecipe recipe) {}
}
