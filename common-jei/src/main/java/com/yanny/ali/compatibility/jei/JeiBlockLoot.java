package com.yanny.ali.compatibility.jei;

import com.yanny.ali.api.RangeValue;
import com.yanny.ali.compatibility.common.BlockLootType;
import com.yanny.ali.registries.LootCategory;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import oshi.util.tuples.Pair;

import java.util.LinkedList;
import java.util.List;

public class JeiBlockLoot extends JeiBaseLoot<BlockLootType, Block> {
    public JeiBlockLoot(IGuiHelper guiHelper, IRecipeType<BlockLootType> recipeType, LootCategory<Block> lootCategory, Component title, IDrawable icon) {
        super(guiHelper, recipeType, lootCategory, title, icon);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BlockLootType recipe, IFocusGroup iFocusGroup) {
        super.setRecipe(builder, recipe, iFocusGroup);
        IRecipeSlotBuilder slotBuilder = builder.addInputSlot().setSlotName("block");

        if (recipe.block() instanceof BushBlock || recipe.block().asItem() == Items.AIR) {
            slotBuilder.setPosition(CATEGORY_WIDTH / 2 - 9, 5).setOutputSlotBackground();
        } else {
            slotBuilder.setPosition(CATEGORY_WIDTH / 2 - 9, 0).setStandardSlotBackground();
        }

        slotBuilder.add(recipe.block());
    }

    @Override
    Pair<List<IRecipeWidget>, List<IRecipeSlotDrawable>> getWidgets(IRecipeExtrasBuilder builder, BlockLootType recipe) {
        List<IRecipeWidget> widgets = new LinkedList<>();
        List<IRecipeSlotDrawable> slotDrawables = new LinkedList<>();

        builder.getRecipeSlots().findSlotByName("block").ifPresent((slotDrawable -> {
            if (recipe.block() instanceof BushBlock || recipe.block().asItem() == Items.AIR) {
                widgets.add(new JeiBlockSlotWidget(slotDrawable, recipe.block(), CATEGORY_WIDTH / 2 - 9, 5));
                slotDrawables.add(slotDrawable);
            } else {
                widgets.add(new JeiLootSlotWidget(slotDrawable, CATEGORY_WIDTH / 2 - 9, 0, new RangeValue(1)));
                slotDrawables.add(slotDrawable);
            }
        }));

        return new Pair<>(widgets, slotDrawables);
    }

    @Override
    int getYOffset(BlockLootType recipe) {
        boolean isSpecial = recipe.block() instanceof BushBlock || recipe.block().asItem() == Items.AIR;
        return isSpecial ? 30 : 22;
    }
}
