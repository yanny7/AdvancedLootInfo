package com.yanny.ali.compatibility.jei;

import com.yanny.ali.api.*;
import com.yanny.ali.compatibility.common.EntityLootType;
import com.yanny.ali.compatibility.common.GenericUtils;
import com.yanny.ali.plugin.client.widget.LootTableWidget;
import com.yanny.ali.registries.LootCategory;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.SpawnEggItem;
import org.jetbrains.annotations.NotNull;

public class JeiEntityLoot extends JeiBaseLoot<EntityLootType, Entity> {
    private static final int WIDGET_SIZE = 36;

    public JeiEntityLoot(IGuiHelper guiHelper, RecipeType<RecipeHolder<EntityLootType>> recipeType, LootCategory<Entity> lootCategory, Component title, IDrawable icon) {
        super(guiHelper, recipeType, lootCategory, title, icon);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<EntityLootType> recipe, IFocusGroup iFocusGroup) {
        super.setRecipe(builder, recipe, iFocusGroup);

        SpawnEggItem spawnEgg = SpawnEggItem.byId(recipe.type().entity().getType());

        if (spawnEgg != null) {
            builder.addSlot(RecipeIngredientRole.CATALYST, 1, 1)
                    .addItemStack(spawnEgg.getDefaultInstance());
        }
    }

    @NotNull
    @Override
    public IDrawable getBackground() {
        return guiHelper.createBlankDrawable(getWidth(), getHeight());
    }

    @Override
    public void draw(RecipeHolder<EntityLootType> recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
        Font font = Minecraft.getInstance().font;

        guiHelper.getSlotDrawable().draw(guiGraphics, 0, 0);
        guiGraphics.drawString(font, recipe.type().entity().getDisplayName(), (9 * 18 - font.width(recipe.type().entity().getDisplayName())) / 2, 0, 0, false);
        GenericUtils.renderEntity(recipe.type().entity(), new Rect((9 * 18 - WIDGET_SIZE) / 2, 10, WIDGET_SIZE, WIDGET_SIZE), 9 * 18, guiGraphics, (int) mouseX, (int) mouseY);
    }

    @Override
    int getYOffset(EntityLootType recipe) {
        return 48;
    }

    @Override
    IWidget getRootWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth) {
        return new LootTableWidget(utils, entry, rect, maxWidth);
    }
}
