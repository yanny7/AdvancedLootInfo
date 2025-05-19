package com.yanny.ali.compatibility.emi;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.Widget;
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
    public int getDisplayHeight() {
        return 10 + getItemsHeight();
    }

    @Override
    protected List<Widget> getAdditionalWidgets(WidgetHolder widgetHolder) {
        Component component = Component.translatableWithFallback("ali/loot_table/" + id.getPath().substring(1), id.getPath());
        return List.of(new TextWidget(component.getVisualOrderText(), 0, 0, 0, false));
    }
}
