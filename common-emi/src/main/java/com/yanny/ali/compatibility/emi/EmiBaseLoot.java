package com.yanny.ali.compatibility.emi;

import com.yanny.ali.api.IWidgetUtils;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.api.Rect;
import com.yanny.ali.plugin.client.ClientUtils;
import com.yanny.ali.plugin.client.widget.LootTableWidget;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class EmiBaseLoot extends BasicEmiRecipe {
    private final Widget widget;
    private final List<Widget> slotWidgets = new LinkedList<>();

    public EmiBaseLoot(EmiRecipeCategory category, ResourceLocation id, LootTable lootTable, int widgetX, int widgetY, List<Item> items) {
        super(category, id, 9 * 18, 1024);
        widget = new EmiWidgetWrapper(new LootTableWidget(getEmiUtils(this), lootTable, widgetX, widgetY));
        outputs.addAll(items.stream().map(EmiStack::of).toList());
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        widgetHolder.add(widget);

        for (Widget slotWidget : slotWidgets) {
            widgetHolder.add(slotWidget);
        }
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

    @NotNull
    private IWidgetUtils getEmiUtils(EmiRecipe recipe) {
        return new ClientUtils() {
            @Override
            public Rect addSlotWidget(Item item, LootPoolEntryContainer entry, int x, int y, Map<Holder<Enchantment>, Map<Integer, RangeValue>> chance,
                                      Map<Holder<Enchantment>, Map<Integer, RangeValue>> count, List<LootItemFunction> allFunctions, List<LootItemCondition> allConditions) {
                EmiLootSlotWidget widget = new EmiLootSlotWidget(this, entry, EmiStack.of(item), x, y, chance, count, allFunctions, allConditions);

                widget.recipeContext(recipe);
                slotWidgets.add(widget);

                Bounds bounds = widget.getBounds();
                return new Rect(bounds.x(), bounds.y(), bounds.width(), bounds.height());
            }

            @Override
            public Rect addSlotWidget(TagKey<Item> item, LootPoolEntryContainer entry, int x, int y, Map<Holder<Enchantment>, Map<Integer, RangeValue>> chance,
                                      Map<Holder<Enchantment>, Map<Integer, RangeValue>> count, List<LootItemFunction> allFunctions, List<LootItemCondition> allConditions) {
                EmiLootSlotWidget widget = new EmiLootSlotWidget(this, entry, EmiIngredient.of(item), x, y, chance, count, allFunctions, allConditions);

                widget.recipeContext(recipe);
                slotWidgets.add(widget);

                Bounds bounds = widget.getBounds();
                return new Rect(bounds.x(), bounds.y(), bounds.width(), bounds.height());
            }
        };
    }
}
