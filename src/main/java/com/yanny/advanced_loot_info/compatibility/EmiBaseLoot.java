package com.yanny.advanced_loot_info.compatibility;

import com.mojang.datafixers.util.Pair;
import com.yanny.advanced_loot_info.loot.LootTableEntry;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

import java.util.Objects;

public abstract class EmiBaseLoot extends BasicEmiRecipe {
    protected final LootTableEntry message;
    protected final ItemGroup itemGroup;
    protected final Pair<GroupWidget, Bounds> widget;

    public EmiBaseLoot(EmiRecipeCategory category, ResourceLocation id, LootTableEntry message, int widgetX, int widgetY) {
        super(category, id, 9 * 18, 1024);
        this.message = message;
        itemGroup = ItemData.parse(message).optimize();
        outputs = ItemGroup.getItems(itemGroup).stream()
                .map((d) -> d.item)
                .filter(Objects::nonNull)
                .map(EmiStack::of)
                .toList();
        catalysts = ItemGroup.getItems(itemGroup).stream()
                .map((d) -> d.tag)
                .filter(Objects::nonNull)
                .map(EmiIngredient::of)
                .toList();
        widget = GroupWidget.createWidget(this, itemGroup, widgetX, widgetY);
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        widgetHolder.add(widget.getFirst());
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
        return widget.getSecond().height();
    }
}
