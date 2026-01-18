package com.yanny.ali.jei.compatibility.jei;

import com.mojang.blaze3d.platform.InputConstants;
import com.yanny.ali.api.Rect;
import com.yanny.ali.compatibility.common.AbstractScrollWidget;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import mezz.jei.api.gui.widgets.ISlottedRecipeWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class JeiScrollWidget extends AbstractScrollWidget implements IRecipeWidget, IJeiInputHandler, ISlottedRecipeWidget {
    private final List<IRecipeWidget> widgets;

    public JeiScrollWidget(Rect rect, int contentHeight, List<IRecipeWidget> widgets) {
        super(rect, contentHeight);
        this.widgets = widgets;
    }

    @NotNull
    @Override
    public ScreenRectangle getArea() {
        return new ScreenRectangle(rect.x(), rect.y(), rect.width(), rect.height());
    }

    @NotNull
    @Override
    public ScreenPosition getPosition() {
        return new ScreenPosition(rect.x(), rect.y());
    }

    @NotNull
    @Override
    public Optional<RecipeSlotUnderMouse> getSlotUnderMouse(double mouseX, double mouseY) {
        float scrollAmount = getScrollAmount();

        for (IRecipeWidget widget : widgets) {
            if (widget instanceof ISlottedRecipeWidget slottedWidget) {
                Optional<RecipeSlotUnderMouse> slotUnderMouse = slottedWidget.getSlotUnderMouse(mouseX, mouseY + scrollAmount);

                if (slotUnderMouse.isPresent()) {
                    return Optional.of(new RecipeSlotUnderMouse(slotUnderMouse.get().slot(), 1, (int) (1 - scrollAmount)));
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public void drawWidget(GuiGraphics guiGraphics, double mouseX, double mouseY) {
        render(guiGraphics, mouseX, mouseY);
    }

    @Override
    public void renderWidgets(GuiGraphics guiGraphics, double mouseX, double mouseY) {
        for (IRecipeWidget widget : widgets) {
            widget.drawWidget(guiGraphics, mouseX, mouseY);
        }
    }

    @Override
    public final boolean handleInput(double mouseX, double mouseY, IJeiUserInput userInput) {
        if (!userInput.isSimulate()) {
            dragOriginY = -1;
        }

        return onScrollbarClicked(mouseX, mouseY, userInput.getKey().getValue());
    }

    @Override
    public final boolean handleMouseScrolled(double mouseX, double mouseY, double scrollDeltaX, double scrollDeltaY) {
        return onMouseScrolled(mouseX, mouseY, scrollDeltaY);
    }

    @Override
    public final boolean handleMouseDragged(double mouseX, double mouseY, InputConstants.Key mouseKey, double dragX, double dragY) {
        return onMouseDragged(mouseX, mouseY, mouseKey.getValue());
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, double mouseX, double mouseY) {
        float scrollAmount = getScrollAmount();
        widgets.forEach((widget) -> widget.getTooltip(tooltip, mouseX, mouseY + scrollAmount));
    }
}
