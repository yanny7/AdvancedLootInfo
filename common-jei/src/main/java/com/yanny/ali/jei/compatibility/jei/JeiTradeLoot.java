package com.yanny.ali.jei.compatibility.jei;

import com.yanny.ali.api.*;
import com.yanny.ali.compatibility.common.AbstractScrollWidget;
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
import oshi.util.tuples.Triplet;

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
        Triplet<Component, Component, Rect> title = GenericUtils.prepareTraderTitle(recipe.type().id(), CATEGORY_WIDTH - AbstractScrollWidget.getScrollbarExtraWidth());

        guiGraphics.drawString(Minecraft.getInstance().font, title.getA(), 0, 0, 0, false);

        if (title.getC().contains((int) mouseX, (int) mouseY)) {
            guiGraphics.renderTooltip(Minecraft.getInstance().font, title.getB(), (int) mouseX, (int) mouseY);
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
