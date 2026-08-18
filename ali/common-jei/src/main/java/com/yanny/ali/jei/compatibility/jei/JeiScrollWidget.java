package com.yanny.ali.jei.compatibility.jei;

import com.mojang.blaze3d.platform.InputConstants;
import com.yanny.aci.api.Rect;
import com.yanny.aci.compatibility.AbstractScrollWidget;
import com.yanny.ali.plugin.client.WidgetUtils;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import mezz.jei.api.gui.widgets.ISlottedRecipeWidget;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class JeiScrollWidget extends AbstractScrollWidget implements IRecipeWidget, IJeiInputHandler, ISlottedRecipeWidget {
    private static final int SLOT_SIZE = 18;

    private final List<IRecipeWidget> widgets;

    public JeiScrollWidget(Rect rect, int contentWidth, int contentHeight, List<IRecipeWidget> widgets) {
        super(rect, contentWidth, contentHeight);
        this.widgets = widgets;
    }

    @NotNull
    @Override
    protected Identifier getTexture() {
        return WidgetUtils.TEXTURE_LOC;
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
        if (!isMouseOverContent(mouseX, mouseY)) {
            return Optional.empty();
        }

        float scrollAmountX = getScrollAmountX();
        float scrollAmountY = getScrollAmountY();

        for (IRecipeWidget widget : widgets) {
            if (widget instanceof ISlottedRecipeWidget slottedWidget) {
                Optional<RecipeSlotUnderMouse> slotUnderMouse = slottedWidget.getSlotUnderMouse(mouseX + scrollAmountX, mouseY + scrollAmountY);

                if (slotUnderMouse.isPresent()) {
                    return Optional.of(new RecipeSlotUnderMouse(slotUnderMouse.get().slot(), (int) (1 - scrollAmountX), (int) (1 - scrollAmountY)));
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public void drawWidget(GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        render(guiGraphics, mouseX, mouseY);
    }

    @Override
    public void renderWidgets(GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        for (IRecipeWidget widget : widgets) {
            if (widget instanceof JeiLootSlotWidget && isOutsideViewport(widget.getPosition().x(), widget.getPosition().y(), SLOT_SIZE, SLOT_SIZE)) {
                continue;
            }

            widget.drawWidget(guiGraphics, mouseX, mouseY);
        }
    }

    @Override
    public final boolean handleInput(double mouseX, double mouseY, IJeiUserInput userInput) {
        if (!userInput.isSimulate()) {
            resetDrag();
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
        if (!isMouseOverContent(mouseX, mouseY)) {
            return;
        }

        float scrollAmountX = getScrollAmountX();
        float scrollAmountY = getScrollAmountY();
        widgets.forEach((widget) -> widget.getTooltip(tooltip, mouseX + scrollAmountX, mouseY + scrollAmountY));
    }
}
