package com.yanny.awi.jei.compatibility.jei;

import com.yanny.aci.api.IWidget;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.api.Rect;
import com.yanny.aci.api.RelativeRect;
import com.yanny.awi.api.IDataNode;
import com.yanny.awi.api.IWidgetUtils;
import com.yanny.awi.compatibility.GenericUtils;
import com.yanny.awi.plugin.client.widget.BiomeWidget;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;

import java.util.LinkedList;
import java.util.List;

public class JeiBiomeLoot extends JeiBaseLoot {
    private static final int TITLE_HEIGHT = 10;
    private static final String SPAWN_SLOT_PREFIX = "spawn_";

    public JeiBiomeLoot(IGuiHelper guiHelper, RecipeType<RecipeHolder> recipeType, Component title, IDrawable icon) {
        super(guiHelper, recipeType, title, icon);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder recipe, IFocusGroup iFocusGroup) {
        super.setRecipe(builder, recipe, iFocusGroup);

        List<GenericUtils.SpawnSlot> slots = GenericUtils.getSpawnSlots(recipe.getEntry(), CATEGORY_WIDTH, TITLE_HEIGHT);

        for (int i = 0; i < slots.size(); i++) {
            GenericUtils.SpawnSlot slot = slots.get(i);

            if (slot.egg() != null) {
                builder.addSlot(RecipeIngredientRole.INPUT)
                        .setStandardSlotBackground()
                        .setSlotName(SPAWN_SLOT_PREFIX + i)
                        .setPosition(slot.x(), slot.y())
                        .addRichTooltipCallback((view, tooltipBuilder) -> tooltipBuilder.addAll(slot.getConditions()))
                        .addItemLike(slot.egg());
            }
        }
    }

    @Override
    Pair<List<IRecipeWidget>, List<IRecipeSlotDrawable>> getWidgets(IRecipeExtrasBuilder builder, RecipeHolder recipe) {
        List<IRecipeWidget> widgets = new LinkedList<>();
        List<IRecipeSlotDrawable> slotDrawables = new LinkedList<>();
        List<GenericUtils.SpawnSlot> slots = GenericUtils.getSpawnSlots(recipe.getEntry(), CATEGORY_WIDTH, TITLE_HEIGHT);

        widgets.add(createTextWidget(Component.translatable("biome." + recipe.getId().getNamespace() + "." + recipe.getId().getPath()), 0, 0, false));

        for (int i = 0; i < slots.size(); i++) {
            GenericUtils.SpawnSlot slot = slots.get(i);

            if (slot.egg() != null) {
                builder.getRecipeSlots().findSlotByName(SPAWN_SLOT_PREFIX + i).ifPresent((slotDrawable) -> {
                    widgets.add(new JeiLootSlotWidget(slotDrawable, slot.x(), slot.y(), new RangeValue(1)));
                    slotDrawables.add(slotDrawable);
                });
            } else {
                widgets.add(new IRecipeWidget() {
                    final Rect rect = new Rect(slot.x(), slot.y(), 18, 18);
                    final ScreenPosition position = new ScreenPosition(0, 0);

                    @Override
                    public void drawWidget(GuiGraphics guiGraphics, double mouseX, double mouseY) {
                        guiHelper.getSlotDrawable().draw(guiGraphics, rect.x(), rect.y());
                        GenericUtils.renderUnknownSpawnEgg(guiGraphics, rect.x(), rect.y());
                    }

                    @Override
                    public void getTooltip(ITooltipBuilder tooltip, double mouseX, double mouseY) {
                        if (rect.contains((int) mouseX, (int) mouseY)) {
                            tooltip.addAll(slot.getTooltip());
                        }
                    }

                    @NotNull
                    @Override
                    public ScreenPosition getPosition() {
                        return position;
                    }
                });
            }
        }

        return new Pair<>(widgets, slotDrawables);
    }

    @Override
    int getYOffset(RecipeHolder recipe) {
        return TITLE_HEIGHT + GenericUtils.getSpawnSlotsHeight(recipe.getEntry(), CATEGORY_WIDTH);
    }

    @Override
    IWidget getRootWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth) {
        return new BiomeWidget(utils, entry, rect, maxWidth);
    }
}
