package com.yanny.advanced_loot_info.compatibility;

import com.yanny.advanced_loot_info.Utils;
import com.yanny.advanced_loot_info.network.LootGroup;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

public class EmiGameplayLoot extends EmiBaseLoot {
    public static final EmiRecipeCategory CHEST_CATEGORY = new EmiRecipeCategory(Utils.modLoc("chest_loot"), EmiStack.of(Items.CHEST));
    public static final EmiRecipeCategory FISHING_CATEGORY = new EmiRecipeCategory(Utils.modLoc("fishing_loot"), EmiStack.of(Items.FISHING_ROD));
    public static final EmiRecipeCategory ARCHAEOLOGY_CATEGORY = new EmiRecipeCategory(Utils.modLoc("archaeology_loot"), EmiStack.of(Items.DECORATED_POT));
    public static final EmiRecipeCategory HERO_CATEGORY = new EmiRecipeCategory(Utils.modLoc("hero_loot"), EmiStack.of(Items.EMERALD));
    public static final EmiRecipeCategory GAMEPLAY_CATEGORY = new EmiRecipeCategory(Utils.modLoc("gameplay_loot"), EmiStack.of(Items.COMPASS));

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
