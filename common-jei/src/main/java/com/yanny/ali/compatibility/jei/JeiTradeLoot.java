package com.yanny.ali.compatibility.jei;

import com.yanny.ali.api.*;
import com.yanny.ali.compatibility.common.GenericUtils;
import com.yanny.ali.compatibility.common.TradeLootType;
import com.yanny.ali.plugin.client.widget.trades.TradeWidget;
import com.yanny.ali.registries.LootCategory;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;

import java.util.List;

public class JeiTradeLoot extends JeiBaseLoot<TradeLootType, String> {
    public JeiTradeLoot(IGuiHelper guiHelper, IRecipeType<RecipeHolder<TradeLootType>> recipeType, LootCategory<String> lootCategory, Component title, IDrawable icon) {
        super(guiHelper, recipeType, lootCategory, title, icon);
    }

    @Override
    Pair<List<IRecipeWidget>, List<IRecipeSlotDrawable>> getWidgets(IRecipeExtrasBuilder builder, TradeLootType recipe) {
        String key = recipe.id().equals("empty") ? "entity.minecraft.wandering_trader" : "entity.minecraft.villager." + recipe.id();
        String id = recipe.id().equals("empty") ? "wandering_trader" : recipe.id();
        Component text = GenericUtils.ellipsis(key, id, CATEGORY_WIDTH);
        Component fullText = Component.translatableWithFallback(key, id);
        Rect rect = new Rect(0, 0, CATEGORY_WIDTH, 8);
        return new Pair<>(List.of(
                createTextWidget(text, 0, 0, false),
                new TooltipWidget(fullText, rect)
        ), List.of());
    }

    @Override
    int getYOffset(TradeLootType recipe) {
        return 10;
    }

    @Override
    IWidget getRootWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth) {
        return new TradeWidget(utils, entry, rect, maxWidth);
    }

    private record TooltipWidget(Component component, Rect rect) implements IRecipeWidget {
        @NotNull
        @Override
        public ScreenPosition getPosition() {
            return new ScreenPosition(rect().x(), rect.y());
        }

        @Override
        public void getTooltip(ITooltipBuilder tooltip, double mouseX, double mouseY) {
            if (rect.contains((int) mouseX, (int) mouseY)) {
                tooltip.add(component);
            }
        }
    }
}
