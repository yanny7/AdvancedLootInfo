package com.yanny.ali.jei.compatibility.jei;

import com.yanny.ali.api.*;
import com.yanny.ali.compatibility.common.AbstractScrollWidget;
import com.yanny.ali.compatibility.common.GenericUtils;
import com.yanny.ali.compatibility.common.TradeLootType;
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
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;
import oshi.util.tuples.Triplet;

import java.util.ArrayList;
import java.util.List;

public class JeiTradeLoot extends JeiBaseLoot<TradeLootType, Identifier> {
    public JeiTradeLoot(IGuiHelper guiHelper, IRecipeType<RecipeHolder<TradeLootType>> recipeType, LootCategory<Identifier> lootCategory, Component title, IDrawable icon) {
        super(guiHelper, recipeType, lootCategory, title, icon);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<TradeLootType> recipe, IFocusGroup iFocusGroup) {
        super.setRecipe(builder, recipe, iFocusGroup);

        if (!recipe.type().pois().isEmpty()) {
            int i = 0;

            for (Block block : recipe.type().pois()) {
                builder.addInputSlot().setSlotName("poi" + i).setPosition(CATEGORY_WIDTH - i * 18 - 18, 0).setStandardSlotBackground().addItemLike(block);
                i++;
            }
        }

        if (!recipe.type().accepts().isEmpty()) {
            int i = 0;
            int yOffset = recipe.type().pois().isEmpty() ? 10 : 20;
            Component t = Component.translatable("ali.util.advanced_loot_info.accepts");
            int width = Minecraft.getInstance().font.width(t);

            for (Item item : recipe.type().accepts()) {
                builder.addInputSlot().setSlotName("accepts" + i).setPosition(width + 2 + i * 18, yOffset).setStandardSlotBackground().addItemLike(item);
                i++;
            }
        }
    }

    @Override
    Pair<List<IRecipeWidget>, List<IRecipeSlotDrawable>> getWidgets(IRecipeExtrasBuilder builder, TradeLootType recipe) {
        Triplet<Component, Component, Rect> title = GenericUtils.prepareTraderTitle(recipe.id(), CATEGORY_WIDTH - AbstractScrollWidget.getScrollbarExtraWidth());
        List<IRecipeWidget> widgets = new ArrayList<>();
        List<IRecipeSlotDrawable> drawables = new ArrayList<>();

        widgets.add(createTextWidget(title.getA(), 0, 0, false));
        widgets.add(new TooltipWidget(title.getB(), title.getC()));

        if (!recipe.pois().isEmpty()) {
            for (int i = 0; i < recipe.pois().size(); i++) {
                int finalI = i;

                builder.getRecipeSlots().findSlotByName("poi" + i).ifPresent((slotDrawable) -> {
                    widgets.add(new JeiLootSlotWidget(slotDrawable, CATEGORY_WIDTH - finalI * 18 - 18, 0, new RangeValue(1)));
                    drawables.add(slotDrawable);
                });
            }
        }

        if (!recipe.accepts().isEmpty()) {
            int yOffset = recipe.pois().isEmpty() ? 10 : 20;
            Component t = Component.translatable("ali.util.advanced_loot_info.accepts");
            int width = Minecraft.getInstance().font.width(t);

            widgets.add(createTextWidget(t, 0, yOffset + 5, false));

            for (int i = 0; i < recipe.accepts().size(); i++) {
                int finalI = i;

                builder.getRecipeSlots().findSlotByName("accepts" + i).ifPresent((slotDrawable) -> {
                    widgets.add(new JeiLootSlotWidget(slotDrawable, width + 2 + finalI * 18, yOffset, new RangeValue(1)));
                    drawables.add(slotDrawable);
                });
            }
        }

        return new Pair<>(widgets, drawables);
    }

    @Override
    int getYOffset(TradeLootType recipe) {
        return (recipe.pois().isEmpty() ? 10 : 20) + (recipe.accepts().isEmpty() ? 0 : 20);
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
