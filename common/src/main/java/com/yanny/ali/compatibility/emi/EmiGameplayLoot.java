package com.yanny.ali.compatibility.emi;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.List;

public class EmiGameplayLoot extends EmiBaseLoot {
    public EmiGameplayLoot(EmiRecipeCategory category, ResourceLocation id, LootTable lootTable, List<Item> items) {
        super(category, id, lootTable, 0, 10, items);
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        widgetHolder.addText(Component.translatableWithFallback("ali/loot_table/" + id.getPath().substring(1), id.getPath()), 0, 0, 0, false);
        super.addWidgets(widgetHolder);
    }

    @Override
    public int getDisplayHeight() {
        return 10 + getItemsHeight();
    }
}
