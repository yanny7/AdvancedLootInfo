package com.yanny.advanced_loot_info.compatibility;

import com.yanny.advanced_loot_info.network.LootTableEntry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class EmiGameplayLoot extends EmiBaseLoot {
    public EmiGameplayLoot(EmiRecipeCategory category, ResourceLocation id, LootTableEntry message) {
        super(category, id, message);
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder, int[] pos) {
        widgetHolder.addText(Component.translatableWithFallback("advanced_loot_info/loot_table/" + id.getPath().substring(1), id.getPath()), 0, 0, 0, false);

        super.addWidgets(widgetHolder, new int[]{0, 10});
    }

    @Override
    public int getDisplayHeight() {
        return 10 + getItemsHeight();
    }
}
