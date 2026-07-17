package com.yanny.awi.emi.compatibility.emi;

import com.yanny.aci.api.IWidget;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.api.Rect;
import com.yanny.aci.api.RelativeRect;
import com.yanny.aci.tooltip.TooltipNodePalette;
import com.yanny.awi.api.IDataNode;
import com.yanny.awi.api.IWidgetUtils;
import com.yanny.awi.compatibility.AbstractScrollWidget;
import com.yanny.awi.manager.PluginManager;
import com.yanny.awi.plugin.client.ClientUtils;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
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

    public EmiBaseLoot(EmiRecipeCategory category, Identifier id, IDataNode lootTable, int widgetX, int widgetY, List<ItemStack> inputs, List<Block> outputs) {
        super(category, Identifier.fromNamespaceAndPath(id.getNamespace(), "/" + id.getPath()), CATEGORY_WIDTH + AbstractScrollWidget.getScrollbarExtraWidth(), 1024);
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
            EmiIngredient ingredient = EmiStack.of(h.block);
            EmiLootSlotWidget widget = new EmiLootSlotWidget(h.entry, ingredient, h.rect.getX(), h.rect.getY(), new RangeValue(1));

            widget.recipeContext(h.recipe);
            return (Widget) widget;
        }).toList());
        widgets.addAll(getAdditionalWidgets(widgetHolder));
        widgets.add(widget);
        widgetHolder.add(new EmiScrollWidget(rect, getDisplayHeight(), widgets));
    }

    @Override
    public RecipeHolder<?> getBackingRecipe() {
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
