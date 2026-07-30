package com.yanny.awi.jei.compatibility.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.yanny.aci.api.Rect;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.gui.widgets.ISlottedRecipeWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class JeiBlockSlotWidget implements ISlottedRecipeWidget {
    private final BlockState blockState;
    private final Block block;
    private final ScreenPosition position;
    private final Rect rect;
    private final ClientLevel level;
    private final IRecipeSlotDrawable slotDrawable;


    public JeiBlockSlotWidget(IRecipeSlotDrawable slotDrawable, Block block, int x, int y) {
        this.slotDrawable = slotDrawable;
        this.block = block;
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
        PoseStack poseStack = guiGraphics.pose();
        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();

        poseStack.translate(1, 1, 0);
        slotDrawable.draw(guiGraphics);
        poseStack.translate(-1, -1, 0);

        poseStack.pushPose();
        poseStack.translate(rect.x(), rect.y(), 0);
        poseStack.translate(15.5, 13.5, 300);
        poseStack.scale(9, -9, 9);
        poseStack.mulPose(Axis.XP.rotationDegrees(30f));
        poseStack.mulPose(Axis.YP.rotationDegrees(225f));
        guiGraphics.drawSpecial((bufferSource) -> blockRenderer.renderSingleBlock(blockState, poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY));
        poseStack.popPose();
    }

    public void getTooltip(ITooltipBuilder tooltip, double mouseX, double mouseY) {
        if (slotDrawable.isMouseOver(mouseX, mouseY)) {
            tooltip.add(Component.translatable(blockState.getBlock().getDescriptionId()));
        }
    }
}
