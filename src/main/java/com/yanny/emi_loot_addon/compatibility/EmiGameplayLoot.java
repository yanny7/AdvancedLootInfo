package com.yanny.emi_loot_addon.compatibility;

import com.yanny.emi_loot_addon.Utils;
import com.yanny.emi_loot_addon.network.LootGroup;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

public class EmiGameplayLoot extends EmiBaseLoot {
    public static final EmiRecipeCategory CATEGORY = new EmiRecipeCategory(Utils.modLoc("gameplay_loot"), EmiStack.of(Items.FISHING_ROD));

    public EmiGameplayLoot(ResourceLocation id, LootGroup message) {
        super(CATEGORY, id, message);
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder, int[] pos) {
        widgetHolder.addText(Component.translatable(id.getPath().substring(1)), 0, 0, 0, false);

        super.addWidgets(widgetHolder, new int[]{0, 10});
    }

    @Override
    public int getDisplayHeight() {
        return 12 + (outputs.size() / 9 + 1) * 18;
    }
}
