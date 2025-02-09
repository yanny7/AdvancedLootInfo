package com.yanny.advanced_loot_info.compatibility.jei;

import com.yanny.advanced_loot_info.loot.LootTableEntry;
import com.yanny.advanced_loot_info.registries.LootCategory;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class JeiGameplayLoot extends JeiBaseLoot<JeiGameplayLoot.GameplayType, String> {
    public JeiGameplayLoot(IGuiHelper guiHelper, RecipeType<GameplayType> recipeType, LootCategory<String> lootCategory, Component title, IDrawable icon) {
        super(recipeType, lootCategory, title, icon);
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, GameplayType recipe, IFocusGroup focuses) {
        super.createRecipeExtras(builder, recipe, focuses);
        builder.addText(Component.translatableWithFallback("advanced_loot_info/loot_table/" + recipe.id.getPath().substring(1), recipe.id.getPath()), 9 * 18, 10)
                .setPosition(0, 0)
                .setColor(0)
                .setShadow(false);
    }

    @Override
    int getYOffset(GameplayType recipe) {
        return 10;
    }

    public record GameplayType(LootTableEntry entry, ResourceLocation id) implements IType {}
}
