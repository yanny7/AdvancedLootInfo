package com.yanny.advanced_loot_info.compatibility;

import com.yanny.advanced_loot_info.network.LootGroup;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class EmiGameplayLoot extends EmiBaseLoot {
    public EmiGameplayLoot(EmiRecipeCategory category, ResourceLocation id, LootGroup message) {
        super(category, id, message);
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder, int[] pos) {
        widgetHolder.addText(Component.translatable("advanced_loot_info/loot_table/" + id.getPath().substring(1)), 0, 0, 0, false);

        super.addWidgets(widgetHolder, new int[]{0, 10});
    }

    @Override
    public int getDisplayHeight() {
        return 12 + (outputs.size() / 9 + 1) * 18;
    }
}
