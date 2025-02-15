package com.yanny.ali.compatibility.jei;

import com.yanny.ali.api.Rect;
import com.yanny.ali.compatibility.common.EntityLootType;
import com.yanny.ali.compatibility.common.GenericUtils;
import com.yanny.ali.registries.LootCategory;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.common.ForgeSpawnEggItem;
import org.jetbrains.annotations.NotNull;

public class JeiEntityLoot extends JeiBaseLoot<EntityLootType, Entity> {
    public JeiEntityLoot(IGuiHelper guiHelper, RecipeType<EntityLootType> recipeType, LootCategory<Entity> lootCategory, Component title, IDrawable icon) {
        super(recipeType, lootCategory, title, icon);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, EntityLootType recipe, IFocusGroup iFocusGroup) {
        super.setRecipe(builder, recipe, iFocusGroup);

        SpawnEggItem spawnEgg = ForgeSpawnEggItem.fromEntityType(recipe.entity().getType());

        if (spawnEgg != null) {
            builder.addSlot(RecipeIngredientRole.CATALYST).setPosition(1, 1).setStandardSlotBackground().addItemLike(spawnEgg);
        }
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, EntityLootType recipe, IFocusGroup focuses) {
        super.createRecipeExtras(builder, recipe, focuses);

        ClientLevel level = Minecraft.getInstance().level;

        if (level != null) {
            int length = Minecraft.getInstance().font.width(recipe.entity().getDisplayName());

            builder.addText(recipe.entity().getDisplayName(), 7 * 18, 10).setPosition((9 * 18 - length) / 2, 0).setColor(0).setShadow(false);
            builder.addWidget(new IRecipeWidget() {
                private static final int WIDGET_SIZE = 36;
                final Rect rect = new Rect((9 * 18 - WIDGET_SIZE) / 2, 10, WIDGET_SIZE, WIDGET_SIZE);
                final ScreenPosition position = new ScreenPosition(0, 0);

                @Override
                public void drawWidget(GuiGraphics guiGraphics, double mouseX, double mouseY) {
                    GenericUtils.renderEntity(recipe.entity(), rect, 9 * 18, guiGraphics, (int) mouseX, (int) mouseY);
                }

                @NotNull
                @Override
                public ScreenPosition getPosition() {
                    return position;
                }
            });
        }
    }

    @Override
    int getYOffset(EntityLootType recipe) {
        return 48;
    }
}
