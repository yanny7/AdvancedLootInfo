package com.yanny.ali.jei.compatibility.jei;

import com.yanny.ali.api.*;
import com.yanny.ali.compatibility.common.AbstractScrollWidget;
import com.yanny.ali.compatibility.common.GameplayLootType;
import com.yanny.ali.compatibility.common.GenericUtils;
import com.yanny.ali.configuration.LootCategory;
import com.yanny.ali.plugin.client.widget.LootTableWidget;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;
import oshi.util.tuples.Triplet;

import java.util.List;

public class JeiGameplayLoot extends JeiBaseLoot<GameplayLootType, ResourceLocation> {
    public JeiGameplayLoot(IGuiHelper guiHelper, RecipeType<RecipeHolder<GameplayLootType>> recipeType, LootCategory<ResourceLocation> lootCategory, Component title, IDrawable icon) {
        super(guiHelper, recipeType, lootCategory, title, icon);
    }

    @Override
    Pair<List<IRecipeWidget>, List<IRecipeSlotDrawable>> getWidgets(IRecipeExtrasBuilder builder, GameplayLootType recipe) {
        Triplet<Component, Component, Rect> title = GenericUtils.prepareGameplayTitle(recipe.id(), CATEGORY_WIDTH - AbstractScrollWidget.getScrollbarExtraWidth());
        return new Pair<>(List.of(createTextWidget(title.getA(), 0, 0, false), new TooltipWidget(title.getB(), title.getC())), List.of());
    }

    @Override
    int getYOffset(GameplayLootType recipe) {
        return 10;
    }

    @Override
    IWidget getRootWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth) {
        return new LootTableWidget(utils, entry, rect, maxWidth);
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
