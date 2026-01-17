package com.yanny.ali.emi.compatibility.emi;

import com.mojang.datafixers.util.Either;
import com.yanny.ali.api.*;
import com.yanny.ali.compatibility.common.AbstractScrollWidget;
import com.yanny.ali.plugin.client.ClientUtils;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public abstract class EmiBaseLoot extends BasicEmiRecipe {
    static final int CATEGORY_WIDTH = 9 * 18 - AbstractScrollWidget.getScrollBoxScrollbarExtraWidth();
    private final Widget widget;
    private final List<Holder> slotWidgets = new LinkedList<>();

    public EmiBaseLoot(EmiRecipeCategory category, ResourceLocation id, IDataNode lootTable, int widgetX, int widgetY, List<ItemStack> inputs, List<ItemStack> outputs) {
        super(category, new ResourceLocation(id.getNamespace(), "/" + id.getPath()), CATEGORY_WIDTH + AbstractScrollWidget.getScrollBoxScrollbarExtraWidth(), 1024);
        RelativeRect rect = new RelativeRect(widgetX, widgetY, CATEGORY_WIDTH, 0);
        widget = new EmiWidgetWrapper(getRootWidget(getEmiUtils(this), lootTable, rect, CATEGORY_WIDTH));
        this.inputs.addAll(inputs.stream().map(EmiStack::of).toList());
        this.outputs.addAll(outputs.stream().map(EmiStack::of).toList());
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        Rect rect = new Rect(0, 0, CATEGORY_WIDTH + AbstractScrollWidget.getScrollBoxScrollbarExtraWidth(), Math.min(getDisplayHeight(), widgetHolder.getHeight()));
        List<Widget> widgets = new ArrayList<>();

        widgets.addAll(slotWidgets.stream().map((h) -> {
            EmiIngredient ingredient = h.item.map(EmiStack::of, EmiIngredient::of);
            EmiLootSlotWidget widget = new EmiLootSlotWidget(h.entry, ingredient, h.rect.getX(), h.rect.getY(), ((IItemNode) h.entry).getCount());

            widget.recipeContext(h.recipe);
            return (Widget) widget;
        }).toList());
        widgets.addAll(getAdditionalWidgets(widgetHolder));
        widgets.add(widget);
        widgetHolder.add(new EmiScrollWidget(rect, getDisplayHeight(), widgets));
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

    abstract IWidget getRootWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth);

    @NotNull
    private IWidgetUtils getEmiUtils(EmiRecipe recipe) {
        return new ClientUtils() {
            @Override
            public void addSlotWidget(Either<ItemStack, TagKey<? extends ItemLike>> item, IDataNode entry, RelativeRect rect) {
                slotWidgets.add(new Holder(this, item, entry, rect, recipe));
            }
        };
    }

    private record Holder(IWidgetUtils utils, Either<ItemStack, TagKey<? extends ItemLike>> item, IDataNode entry, RelativeRect rect, EmiRecipe recipe) {}
}
