package com.yanny.advanced_loot_info.compatibility.jei;

import com.yanny.advanced_loot_info.compatibility.common.GameplayLootType;
import com.yanny.advanced_loot_info.registries.LootCategory;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.network.chat.Component;

public class JeiGameplayLoot extends JeiBaseLoot<GameplayLootType, String> {
    public JeiGameplayLoot(IGuiHelper guiHelper, RecipeType<GameplayLootType> recipeType, LootCategory<String> lootCategory, Component title, IDrawable icon) {
        super(recipeType, lootCategory, title, icon);
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, GameplayLootType recipe, IFocusGroup focuses) {
        super.createRecipeExtras(builder, recipe, focuses);
        builder.addText(Component.translatableWithFallback("advanced_loot_info/loot_table/" + recipe.id().substring(1), recipe.id()), 9 * 18, 10)
                .setPosition(0, 0)
                .setColor(0)
                .setShadow(false);
    }

    @Override
    int getYOffset(GameplayLootType recipe) {
        return 10;
    }
}
