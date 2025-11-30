package com.yanny.ali.compatibility.jei;

import com.yanny.ali.api.*;
import com.yanny.ali.compatibility.common.GameplayLootType;
import com.yanny.ali.compatibility.common.GenericUtils;
import com.yanny.ali.configuration.LootCategory;
import com.yanny.ali.plugin.client.widget.LootTableWidget;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class JeiGameplayLoot extends JeiBaseLoot<GameplayLootType, ResourceLocation> {
    public JeiGameplayLoot(IGuiHelper guiHelper, RecipeType<RecipeHolder<GameplayLootType>> recipeType, LootCategory<ResourceLocation> lootCategory, Component title, IDrawable icon) {
        super(guiHelper, recipeType, lootCategory, title, icon);
    }

    @NotNull
    @Override
    public IDrawable getBackground() {
        return guiHelper.createBlankDrawable(getWidth(), getHeight());
    }

    @Override
    public void draw(RecipeHolder<GameplayLootType> recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
        String key = "ali/loot_table/" + recipe.type().id().getPath();
        Component text = GenericUtils.ellipsis(key, recipe.type().id().getPath(), CATEGORY_WIDTH);
        Component fullText = Component.literal(recipe.type().id().toString());
        Rect rect = new Rect(0, 0, 9 * 18, 8);

        guiGraphics.drawString(Minecraft.getInstance().font, text, 0, 0, 0, false);

        if (rect.contains((int) mouseX, (int) mouseY)) {
            guiGraphics.renderTooltip(Minecraft.getInstance().font, fullText, (int) mouseX, (int) mouseY);
        }
    }

    @Override
    int getYOffset(GameplayLootType recipe) {
        return 10;
    }

    @Override
    IWidget getRootWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth) {
        return new LootTableWidget(utils, entry, rect, maxWidth);
    }
}
