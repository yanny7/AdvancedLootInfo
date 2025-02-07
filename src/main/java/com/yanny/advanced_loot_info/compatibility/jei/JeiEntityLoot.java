package com.yanny.advanced_loot_info.compatibility.jei;

import com.yanny.advanced_loot_info.loot.LootTableEntry;
import com.yanny.advanced_loot_info.registries.LootCategory;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

public class JeiEntityLoot extends BaseRecipeCategory<JeiEntityLoot.EntityType, Entity> {
    public JeiEntityLoot(RecipeType<EntityType> recipeType, LootCategory<Entity> lootCategory, Component title, IDrawable icon) {
        super(recipeType, lootCategory, title, icon);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder iRecipeLayoutBuilder, EntityType blockType, IFocusGroup iFocusGroup) {
        super.setRecipe(iRecipeLayoutBuilder, blockType, iFocusGroup);
    }

    public record EntityType(Entity entity, LootTableEntry entry) implements IType {}
}
