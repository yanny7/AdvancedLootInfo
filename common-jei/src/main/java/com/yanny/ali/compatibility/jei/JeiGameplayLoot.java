package com.yanny.ali.compatibility.jei;

import com.yanny.ali.api.Rect;
import com.yanny.ali.compatibility.common.GameplayLootType;
import com.yanny.ali.compatibility.common.GenericUtils;
import com.yanny.ali.registries.LootCategory;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class JeiGameplayLoot extends JeiBaseLoot<GameplayLootType, String> {
    public JeiGameplayLoot(IGuiHelper guiHelper, RecipeType<GameplayLootType> recipeType, LootCategory<String> lootCategory, Component title, IDrawable icon) {
        super(guiHelper, recipeType, lootCategory, title, icon);
    }

    @NotNull
    @Override
    public IDrawable getBackground() {
        return guiHelper.createBlankDrawable(getWidth(), getHeight());
    }

    @Override
    public void draw(GameplayLootType recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
        String key = "ali/loot_table/" + recipe.id().substring(1);
        Component text = GenericUtils.ellipsis(key, recipe.id(), 9 * 18);
        Component fullText = Component.translatableWithFallback(key, recipe.id());
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
}
