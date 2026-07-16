package com.yanny.awi.jei.compatibility.jei;

import com.yanny.aci.api.IWidget;
import com.yanny.aci.api.RelativeRect;
import com.yanny.awi.api.IDataNode;
import com.yanny.awi.api.IWidgetUtils;
import com.yanny.awi.plugin.client.widget.BiomeWidget;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.network.chat.Component;
import oshi.util.tuples.Pair;

import java.util.List;

public class JeiBiomeLoot extends JeiBaseLoot {
    public JeiBiomeLoot(IGuiHelper guiHelper, RecipeType<RecipeHolder> recipeType, Component title, IDrawable icon) {
        super(guiHelper, recipeType, title, icon);
    }

    @Override
    Pair<List<IRecipeWidget>, List<IRecipeSlotDrawable>> getWidgets(IRecipeExtrasBuilder builder, RecipeHolder recipe) {
        return new Pair<>(List.of(createTextWidget(Component.translatable("biome." + recipe.getId().getNamespace() + "." + recipe.getId().getPath()), 0, 0, false)), List.of());
    }

    @Override
    int getYOffset() {
        return 10;
    }

    @Override
    IWidget getRootWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth) {
        return new BiomeWidget(utils, entry, rect, maxWidth);
    }
}
