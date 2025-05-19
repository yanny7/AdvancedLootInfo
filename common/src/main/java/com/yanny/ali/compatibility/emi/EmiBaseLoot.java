package com.yanny.ali.compatibility.emi;

import com.yanny.ali.api.IWidgetUtils;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.api.Rect;
import com.yanny.ali.plugin.client.ClientUtils;
import com.yanny.ali.plugin.client.TooltipUtils;
import com.yanny.ali.plugin.client.widget.LootTableWidget;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class EmiBaseLoot extends BasicEmiRecipe {
    static final int CATEGORY_WIDTH = 9 * 18 - EmiScrollWidget.getScrollBoxScrollbarExtraWidth();
    private final Widget widget;
    private final List<Widget> slotWidgets = new LinkedList<>();

    public EmiBaseLoot(EmiRecipeCategory category, ResourceLocation id, LootTable lootTable, int widgetX, int widgetY, List<Item> items) {
        super(category, id, CATEGORY_WIDTH + EmiScrollWidget.getScrollBoxScrollbarExtraWidth(), 1024);
        widget = new EmiWidgetWrapper(new LootTableWidget(getEmiUtils(this), lootTable, widgetX, widgetY, CATEGORY_WIDTH));
        outputs.addAll(items.stream().map(EmiStack::of).toList());
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        Rect rect = new Rect(0, 0, CATEGORY_WIDTH + EmiScrollWidget.getScrollBoxScrollbarExtraWidth(), Math.min(getDisplayHeight(), widgetHolder.getHeight()));
        List<Widget> widgets = new LinkedList<>(slotWidgets);

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

    @NotNull
    private IWidgetUtils getEmiUtils(EmiRecipe recipe) {
        return new ClientUtils() {
            @Override
            public Rect addSlotWidget(Item item, LootPoolEntryContainer entry, int x, int y, Map<Enchantment, Map<Integer, RangeValue>> chance,
                                      Map<Enchantment, Map<Integer, RangeValue>> count, List<LootItemFunction> allFunctions, List<LootItemCondition> allConditions) {
                ItemStack itemStack = TooltipUtils.getItemStack(this, entry, item);
                EmiLootSlotWidget widget = new EmiLootSlotWidget(this, entry, EmiStack.of(itemStack), x, y, chance, count, allFunctions, allConditions);

                widget.recipeContext(recipe);
                slotWidgets.add(widget);

                Bounds bounds = widget.getBounds();
                return new Rect(bounds.x(), bounds.y(), bounds.width(), bounds.height());
            }

            @Override
            public Rect addSlotWidget(TagKey<Item> item, LootPoolEntryContainer entry, int x, int y, Map<Enchantment, Map<Integer, RangeValue>> chance,
                                      Map<Enchantment, Map<Integer, RangeValue>> count, List<LootItemFunction> allFunctions, List<LootItemCondition> allConditions) {
                EmiLootSlotWidget widget = new EmiLootSlotWidget(this, entry, EmiIngredient.of(item), x, y, chance, count, allFunctions, allConditions);

                widget.recipeContext(recipe);
                slotWidgets.add(widget);

                Bounds bounds = widget.getBounds();
                return new Rect(bounds.x(), bounds.y(), bounds.width(), bounds.height());
            }
        };
    }
}
