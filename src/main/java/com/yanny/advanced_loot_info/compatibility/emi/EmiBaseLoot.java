package com.yanny.advanced_loot_info.compatibility.emi;

import com.mojang.datafixers.util.Pair;
import com.yanny.advanced_loot_info.api.*;
import com.yanny.advanced_loot_info.loot.LootTableEntry;
import com.yanny.advanced_loot_info.manager.PluginManager;
import com.yanny.advanced_loot_info.plugin.entry.EmptyEntry;
import com.yanny.advanced_loot_info.plugin.entry.ItemEntry;
import com.yanny.advanced_loot_info.plugin.entry.TagEntry;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
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
        widget = new WidgetWrapper(new LootTableWidget(getEmiUtils(this), message, widgetX, widgetY));
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
            public Rect addSlotWidget(ILootEntry entry, int x, int y, RangeValue chance, @Nullable Pair<Enchantment, Map<Integer, RangeValue>> bonusChance, RangeValue count,
                                         @Nullable Pair<Enchantment, Map<Integer, RangeValue>> bonusCount, List<ILootFunction> allFunctions, List<ILootCondition> allConditions) {
                LootSlotWidget widget;

                if (entry instanceof ItemEntry) {
                    widget = new LootSlotWidget((ItemEntry) entry, EmiStack.of(((ItemEntry) entry).item), x, y, chance, bonusChance, count, bonusCount, allFunctions, allConditions);
                    widget.recipeContext(recipe);
                } else if (entry instanceof TagEntry) {
                    widget = new LootSlotWidget((TagEntry) entry, EmiIngredient.of(((TagEntry) entry).item), x, y, chance, bonusChance, count, bonusCount, allFunctions, allConditions);
                    widget.recipeContext(recipe);
                } else if (entry instanceof EmptyEntry) {
                    widget = new LootSlotWidget((EmptyEntry) entry, EmiStack.of(Items.BARRIER), x, y, new RangeValue(), null, new RangeValue(), null, allFunctions, allConditions);
                    widget.drawBack(false);
                } else {
                    throw new IllegalStateException(); //FIXME
                }

                slotWidgets.add(widget);

                Bounds bounds = widget.getBounds();
                return new Rect(bounds.x(), bounds.y(), bounds.width(), bounds.height());
            }
        };
    }
}
