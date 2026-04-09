package com.yanny.ali.jei.compatibility.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.api.Rect;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.gui.widgets.ISlottedRecipeWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class JeiLootSlotWidget implements ISlottedRecipeWidget {
    private final Rect rect;
    private final IRecipeSlotDrawable slotDrawable;
    @Nullable
    private Component count;
    private boolean isRange = false;

    public JeiLootSlotWidget(IRecipeSlotDrawable slotDrawable, int x, int y, RangeValue count) {
        this.slotDrawable = slotDrawable;
        rect = new Rect(x, y, 18, 18);
        setCount(count);
    }

    @NotNull
    @Override
    public Optional<RecipeSlotUnderMouse> getSlotUnderMouse(double mouseX, double mouseY) {
        if (slotDrawable.isMouseOver(mouseX, mouseY)) {
            return Optional.of(new RecipeSlotUnderMouse(slotDrawable, 0, 0));
        }

        return Optional.empty();
    }

    @NotNull
    @Override
    public ScreenPosition getPosition() {
        return new ScreenPosition(rect.x(), rect.y());
    }

    @Override
    public void drawWidget(GuiGraphics guiGraphics, double mouseX, double mouseY) {
        PoseStack stack = guiGraphics.pose();

        stack.translate(1, 1, 0);
        slotDrawable.draw(guiGraphics);
        stack.translate(-1, -1, 0);

        if (count != null) {
            Font font = Minecraft.getInstance().font;

            stack.pushPose();
            stack.translate(rect.x(), rect.y(), 0);

            if (isRange) {
                stack.translate(17, 13, 200);
                stack.pushPose();
                stack.scale(0.5f, 0.5f, 0.5f);
                guiGraphics.drawString(font, count, -font.width(count), 0, 16777215, false);
                stack.popPose();
            } else {
                stack.translate(18, 10, 200);
                guiGraphics.drawString(font, count, -font.width(count), 0, 16777215, true);
            }

            stack.popPose();
        }
    }

    public void getTooltip(ITooltipBuilder tooltip, double mouseX, double mouseY) {
        if (slotDrawable.isMouseOver(mouseX, mouseY)) {
            slotDrawable.getTooltip(tooltip);
        }
    }

    private void setCount(RangeValue count) {
        if (count.isRange() || count.min() > 1) {
            this.count = Component.literal(count.toIntString());
            isRange = count.isRange();
        }
    }
}
