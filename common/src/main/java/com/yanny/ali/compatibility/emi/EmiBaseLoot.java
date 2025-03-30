package com.yanny.ali.compatibility.emi;

import com.mojang.datafixers.util.Pair;
import com.yanny.ali.api.*;
import com.yanny.ali.plugin.Utils;
import com.yanny.ali.plugin.entry.LootTableEntry;
import com.yanny.ali.plugin.widget.LootTableWidget;
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
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class EmiBaseLoot extends BasicEmiRecipe {
    private final Widget widget;
    private final List<Widget> slotWidgets = new LinkedList<>();

    public EmiBaseLoot(EmiRecipeCategory category, ResourceLocation id, LootTableEntry message, int widgetX, int widgetY) {
        super(category, id, 9 * 18, 1024);
        widget = new EmiWidgetWrapper(new LootTableWidget(getEmiUtils(this), message, widgetX, widgetY));
        outputs.addAll(message.collectItems().stream().map(EmiStack::of).toList());
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        widgetHolder.add(widget);

        for (Widget slotWidget : slotWidgets) {
            widgetHolder.add(slotWidget);
        }
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

    @NotNull
    private IWidgetUtils getEmiUtils(EmiRecipe recipe) {
        return new Utils() {
            @Override
            public Rect addSlotWidget(Item item, ILootEntry entry, int x, int y, RangeValue chance, @Nullable Pair<Enchantment, Map<Integer, RangeValue>> bonusChance, RangeValue count,
                                      @Nullable Pair<Enchantment, Map<Integer, RangeValue>> bonusCount, List<ILootFunction> allFunctions, List<ILootCondition> allConditions) {
                EmiLootSlotWidget widget = new EmiLootSlotWidget(this, entry, EmiStack.of(item), x, y, chance, bonusChance, count, bonusCount, allFunctions, allConditions);

                widget.recipeContext(recipe);
                slotWidgets.add(widget);

                Bounds bounds = widget.getBounds();
                return new Rect(bounds.x(), bounds.y(), bounds.width(), bounds.height());
            }

            @Override
            public Rect addSlotWidget(TagKey<Item> item, ILootEntry entry, int x, int y, RangeValue chance, @Nullable Pair<Enchantment, Map<Integer, RangeValue>> bonusChance, RangeValue count,
                                      @Nullable Pair<Enchantment, Map<Integer, RangeValue>> bonusCount, List<ILootFunction> allFunctions, List<ILootCondition> allConditions) {
                EmiLootSlotWidget widget = new EmiLootSlotWidget(this, entry, EmiIngredient.of(item), x, y, chance, bonusChance, count, bonusCount, allFunctions, allConditions);

                widget.recipeContext(recipe);
                slotWidgets.add(widget);

                Bounds bounds = widget.getBounds();
                return new Rect(bounds.x(), bounds.y(), bounds.width(), bounds.height());
            }
        };
    }
}
