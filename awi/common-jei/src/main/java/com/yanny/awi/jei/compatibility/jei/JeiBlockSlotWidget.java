package com.yanny.awi.jei.compatibility.jei;

import com.yanny.aci.api.Rect;
import com.yanny.aci.compatibility.AbstractScrollWidget;
import com.yanny.awi.pip.BlockRenderState;
import com.yanny.awi.platform.Services;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.gui.widgets.ISlottedRecipeWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2fStack;

import java.util.Optional;

public class JeiBlockSlotWidget implements ISlottedRecipeWidget {
    /** Block edge length in GUI pixels - fits an 18px slot with a pixel of margin. */
    private static final float BLOCK_SIZE = 9;

    private final BlockState blockState;
    private final ScreenPosition position;
    private final Rect rect;
    private final ClientLevel level;
    private final IRecipeSlotDrawable slotDrawable;


    public JeiBlockSlotWidget(IRecipeSlotDrawable slotDrawable, Block block, int x, int y) {
        this.slotDrawable = slotDrawable;
        blockState = block.defaultBlockState();
        position = new ScreenPosition(x, y);
        rect = new Rect(x, y, 18, 18);
        level = Minecraft.getInstance().level;
    }

    @NotNull
    @Override
    public Optional<RecipeSlotUnderMouse> getSlotUnderMouse(double mouseX, double mouseY) {
        return Optional.empty();
    }

    @NotNull
    @Override
    public ScreenPosition getPosition() {
        return position;
    }

    @Override
    public void drawWidget(GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Matrix3x2fStack poseStack = guiGraphics.pose();

        poseStack.translate(1, 1);
        slotDrawable.draw(guiGraphics);
        poseStack.translate(-1, -1);

        // The scroll widget draws its children itself, so the pose is not translated to this widget's position -
        // it has to be applied here before the absolute render position is read off the pose.
        poseStack.pushMatrix();
        poseStack.translate(rect.x(), rect.y());

        int centerX = (int) poseStack.m20() + rect.width() / 2;
        int centerY = (int) poseStack.m21() + rect.height() / 2;

        BlockRenderState renderState = BlockRenderState.centered(blockState, level, centerX, centerY, BLOCK_SIZE, AbstractScrollWidget.getCurrentViewport());
        Services.getClientPlatform().renderBlockInGui(guiGraphics, renderState);

        poseStack.popMatrix();
    }

    public void getTooltip(ITooltipBuilder tooltip, double mouseX, double mouseY) {
        if (slotDrawable.isMouseOver(mouseX, mouseY)) {
            tooltip.add(Component.translatable(blockState.getBlock().getDescriptionId()));
        }
    }
}
