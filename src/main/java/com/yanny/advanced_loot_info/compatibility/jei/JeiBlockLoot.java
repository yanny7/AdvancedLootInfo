package com.yanny.advanced_loot_info.compatibility.jei;

import com.yanny.advanced_loot_info.loot.LootTableEntry;
import com.yanny.advanced_loot_info.registries.LootCategory;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.IPlantable;

public class JeiBlockLoot extends BaseRecipeCategory<JeiBlockLoot.BlockType, Block> {
    public JeiBlockLoot(RecipeType<BlockType> recipeType, LootCategory<Block> lootCategory, Component title, IDrawable icon) {
        super(recipeType, lootCategory, title, icon);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BlockType blockType, IFocusGroup iFocusGroup) {
        super.setRecipe(builder, blockType, iFocusGroup);
        boolean isSpecial = blockType.block instanceof IPlantable || blockType.block.asItem() == Items.AIR;
        builder.addInputSlot().addItemLike(blockType.block);
    }

    public record BlockType(Block block, LootTableEntry entry) implements IType {}
}
