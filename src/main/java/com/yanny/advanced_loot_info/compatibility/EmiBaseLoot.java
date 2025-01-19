package com.yanny.advanced_loot_info.compatibility;

import com.mojang.datafixers.util.Pair;
import com.yanny.advanced_loot_info.network.LootTableEntry;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class EmiBaseLoot extends BasicEmiRecipe {
    protected final LootTableEntry message;
    protected final ItemGroup itemGroup;
    @Nullable
    private Bounds bounds = null;

    public EmiBaseLoot(EmiRecipeCategory category, ResourceLocation id, LootTableEntry message) {
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
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        addWidgets(widgetHolder, new int[]{0, 0});
    }

    @Override
    public Recipe<?> getBackingRecipe() {
        return null;
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    public void addWidgets(WidgetHolder widgetHolder, int[] pos) {
        Pair<GroupWidget, Bounds> widget = GroupWidget.createWidget(this, itemGroup, pos[0], pos[1]);
        widgetHolder.add(widget.getFirst());
        bounds = widget.getSecond();
    }

    protected int getItemsHeight() {
        return bounds != null ? bounds.height() : 1024;
    }
}
