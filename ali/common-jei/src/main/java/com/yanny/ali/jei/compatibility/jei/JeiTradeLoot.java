package com.yanny.ali.jei.compatibility.jei;

import com.yanny.aci.api.IWidget;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.api.Rect;
import com.yanny.aci.api.RelativeRect;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IWidgetUtils;
import com.yanny.ali.compatibility.common.GenericUtils;
import com.yanny.ali.compatibility.common.TradeLootType;
import com.yanny.ali.compatibility.common.TraderHeader;
import com.yanny.ali.configuration.LootCategory;
import com.yanny.ali.plugin.client.widget.trades.TradeWidget;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class JeiTradeLoot extends JeiBaseLoot<TradeLootType, Identifier> {
    private static final String SPAWN_EGG = "spawn_egg";
    private static final String POI = "poi";
    private static final String ACCEPTS = "accepts";

    public JeiTradeLoot(IGuiHelper guiHelper, IRecipeType<RecipeHolder<TradeLootType>> recipeType, LootCategory<Identifier> lootCategory, Component title, IDrawable icon) {
        super(guiHelper, recipeType, lootCategory, title, icon);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<TradeLootType> recipe, IFocusGroup iFocusGroup) {
        super.setRecipe(builder, recipe, iFocusGroup);

        TraderHeader header = getHeader(recipe.type());

        if (header.spawnEgg() != null) {
            Rect rect = header.spawnEggRect();
            builder.addSlot(RecipeIngredientRole.CRAFTING_STATION).setSlotName(SPAWN_EGG).setPosition(rect.x(), rect.y()).setStandardSlotBackground().add(header.spawnEgg());
        }

        addInputSlots(builder, POI, recipe.type().pois(), header.pois());
        addInputSlots(builder, ACCEPTS, recipe.type().accepts(), header.accepts());
    }

    @Override
    Pair<List<IRecipeWidget>, List<IRecipeSlotDrawable>> getWidgets(IRecipeExtrasBuilder builder, TradeLootType recipe) {
        TraderHeader header = getHeader(recipe);
        List<IRecipeWidget> widgets = new ArrayList<>();
        List<IRecipeSlotDrawable> drawables = new ArrayList<>();
        Rect titleRect = header.titleRect();

        addSlotWidget(builder, widgets, drawables, SPAWN_EGG, header.spawnEggRect());
        widgets.add(createTextWidget(header.title(), titleRect.x(), titleRect.y(), false));

        if (header.titleTooltip() != null) {
            widgets.add(new TooltipWidget(header.titleTooltip(), titleRect));
        }

        addSlotWidgets(builder, widgets, drawables, POI, header.pois());

        if (!recipe.accepts().isEmpty()) {
            widgets.add(createTextWidget(header.acceptsLabel(), header.acceptsLabelRect().x(), header.acceptsLabelRect().y(), false));
            addSlotWidgets(builder, widgets, drawables, ACCEPTS, header.accepts());
        }

        return new Pair<>(widgets, drawables);
    }

    @Override
    int getYOffset(TradeLootType recipe) {
        return getHeader(recipe).height();
    }

    @Override
    int getMinContentHeight(TradeLootType recipe) {
        return getHeader(recipe).minContentHeight();
    }

    @NotNull
    private static TraderHeader getHeader(TradeLootType recipe) {
        return GenericUtils.prepareTraderHeader(recipe, CATEGORY_WIDTH);
    }

    private static void addInputSlots(IRecipeLayoutBuilder builder, String name, Set<? extends ItemLike> items, List<Rect> rects) {
        int i = 0;

        for (ItemLike item : items) {
            Rect rect = rects.get(i);
            builder.addInputSlot().setSlotName(name + i).setPosition(rect.x(), rect.y()).setStandardSlotBackground().add(item);
            i++;
        }
    }

    private static void addSlotWidgets(IRecipeExtrasBuilder builder, List<IRecipeWidget> widgets, List<IRecipeSlotDrawable> drawables, String name, List<Rect> rects) {
        for (int i = 0; i < rects.size(); i++) {
            addSlotWidget(builder, widgets, drawables, name + i, rects.get(i));
        }
    }

    private static void addSlotWidget(IRecipeExtrasBuilder builder, List<IRecipeWidget> widgets, List<IRecipeSlotDrawable> drawables, String slotName, Rect rect) {
        builder.getRecipeSlots().findSlotByName(slotName).ifPresent((slotDrawable) -> {
            widgets.add(new JeiLootSlotWidget(slotDrawable, rect.x(), rect.y(), new RangeValue(1)));
            drawables.add(slotDrawable);
        });
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
