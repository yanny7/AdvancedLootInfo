package com.yanny.ali.compatibility.jei;

import com.yanny.ali.compatibility.common.BlockLootType;
import com.yanny.ali.registries.LootCategory;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;

public class JeiBlockLoot extends JeiBaseLoot<BlockLootType, Block> {
    private final IGuiHelper guiHelper;

    public JeiBlockLoot(IGuiHelper guiHelper, RecipeType<BlockLootType> recipeType, LootCategory<Block> lootCategory, Component title, IDrawable icon) {
        super(recipeType, lootCategory, title, icon);
        this.guiHelper = guiHelper;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BlockLootType recipe, IFocusGroup iFocusGroup) {
        super.setRecipe(builder, recipe, iFocusGroup);

        boolean isSpecial = recipe.block() instanceof BushBlock || recipe.block().asItem() == Items.AIR;

        if (!isSpecial) {
            builder.addInputSlot(4 * 18 + 1, 1).setStandardSlotBackground().addItemLike(recipe.block());
        }
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, BlockLootType recipe, IFocusGroup focuses) {
        super.createRecipeExtras(builder, recipe, focuses);

        boolean isSpecial = recipe.block() instanceof BushBlock || recipe.block().asItem() == Items.AIR;

        if (isSpecial) {
            builder.addSlottedWidget(new JeiBlockSlotWidget(guiHelper, recipe.block(), 4 * 18, 6), builder.getRecipeSlots().getSlots(RecipeIngredientRole.INPUT));
        }
    }

    @Override
    int getYOffset(BlockLootType recipe) {
        boolean isSpecial = recipe.block() instanceof BushBlock || recipe.block().asItem() == Items.AIR;
        return isSpecial ? 30 : 22;
    }
}
