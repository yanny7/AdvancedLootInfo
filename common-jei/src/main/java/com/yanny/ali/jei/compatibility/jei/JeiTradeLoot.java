package com.yanny.ali.jei.compatibility.jei;

import com.yanny.ali.api.*;
import com.yanny.ali.compatibility.common.GenericUtils;
import com.yanny.ali.compatibility.common.TradeLootType;
import com.yanny.ali.configuration.LootCategory;
import com.yanny.ali.plugin.client.widget.trades.TradeWidget;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class JeiTradeLoot extends JeiBaseLoot<TradeLootType, ResourceLocation> {
    public JeiTradeLoot(IGuiHelper guiHelper, RecipeType<RecipeHolder<TradeLootType>> recipeType, LootCategory<ResourceLocation> lootCategory, Component title, IDrawable icon) {
        super(guiHelper, recipeType, lootCategory, title, icon);
    }

    @NotNull
    @Override
    public IDrawable getBackground() {
        return guiHelper.createBlankDrawable(getWidth(), getHeight());
    }

    @Override
    public void draw(RecipeHolder<TradeLootType> recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
        String key = recipe.type().id().equals("empty") ? "entity.minecraft.wandering_trader" : "entity.minecraft.villager." + recipe.type().id();
        String id = recipe.type().id().equals("empty") ? "wandering_trader" : recipe.type().id();
        Component text = GenericUtils.ellipsis(key, id, 9 * 18);
        Component fullText = Component.translatableWithFallback(key, id);
        Rect rect = new Rect(0, 0, 9 * 18, 8);

        guiGraphics.drawString(Minecraft.getInstance().font, text, 0, 0, 0, false);

        if (rect.contains((int) mouseX, (int) mouseY)) {
            guiGraphics.renderTooltip(Minecraft.getInstance().font, fullText, (int) mouseX, (int) mouseY);
        }
    }

    @Override
    int getYOffset(TradeLootType recipe) {
        return 10;
    }

    @Override
    IWidget getRootWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth) {
        return new TradeWidget(utils, entry, rect, maxWidth);
    }
}
