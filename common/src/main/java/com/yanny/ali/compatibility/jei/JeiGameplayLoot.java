package com.yanny.ali.compatibility.jei;

import com.yanny.ali.compatibility.common.GameplayLootType;
import com.yanny.ali.registries.LootCategory;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.network.chat.Component;
import oshi.util.tuples.Pair;

import java.util.List;

public class JeiGameplayLoot extends JeiBaseLoot<GameplayLootType, String> {
    public JeiGameplayLoot(IGuiHelper guiHelper, RecipeType<GameplayLootType> recipeType, LootCategory<String> lootCategory, Component title, IDrawable icon) {
        super(guiHelper, recipeType, lootCategory, title, icon);
    }

    @Override
    Pair<List<IRecipeWidget>, List<IRecipeSlotDrawable>> getWidgets(IRecipeExtrasBuilder builder, GameplayLootType recipe) {
        Component text = Component.translatableWithFallback("ali/loot_table/" + recipe.id().substring(1), recipe.id());
        return new Pair<>(List.of(createTextWidget(text, 0, 0, false)), List.of());
    }

    @Override
    int getYOffset(GameplayLootType recipe) {
        return 10;
    }
}
