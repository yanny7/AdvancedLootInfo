package com.yanny.advanced_loot_info.compatibility;

import com.yanny.advanced_loot_info.loot.LootTableEntry;
import com.yanny.advanced_loot_info.manager.PluginManager;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

public abstract class EmiBaseLoot extends BasicEmiRecipe {
    private final Widget widget;

    public EmiBaseLoot(EmiRecipeCategory category, ResourceLocation id, LootTableEntry message, int widgetX, int widgetY) {
        super(category, id, 9 * 18, 1024);
        widget = new LootTableWidget(this, PluginManager.REGISTRY, message, widgetX, widgetY);
        outputs.addAll(message.collectItems().stream().map(EmiStack::of).toList());
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        widgetHolder.add(widget);
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
}
