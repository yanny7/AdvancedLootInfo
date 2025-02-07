package com.yanny.advanced_loot_info.compatibility.jei;

import com.yanny.advanced_loot_info.loot.LootTableEntry;
import com.yanny.advanced_loot_info.registries.LootCategory;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.network.chat.Component;

public class JeiGameplayLoot extends BaseRecipeCategory<JeiGameplayLoot.GameplayType, String> {
    public JeiGameplayLoot(RecipeType<GameplayType> recipeType, LootCategory<String> lootCategory, Component title, IDrawable icon) {
        super(recipeType, lootCategory, title, icon);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder iRecipeLayoutBuilder, GameplayType blockType, IFocusGroup iFocusGroup) {
        super.setRecipe(iRecipeLayoutBuilder, blockType, iFocusGroup);
    }

    public record GameplayType(LootTableEntry entry) implements IType {}
}
