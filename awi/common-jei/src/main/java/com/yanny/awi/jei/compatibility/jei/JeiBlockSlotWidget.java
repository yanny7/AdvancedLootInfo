package com.yanny.awi.jei.compatibility.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.yanny.aci.api.Rect;
import com.yanny.aci.compatibility.GuiGraphics;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.gui.widgets.ISlottedRecipeWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.Rect2i;
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
    private final Rect2i position;
    private final Rect rect;
    private final ClientLevel level;
    private final IRecipeSlotDrawable slotDrawable;


    public JeiBlockSlotWidget(IRecipeSlotDrawable slotDrawable, Block block, int x, int y) {
        this.slotDrawable = slotDrawable;
        this.block = block;
        blockState = block.defaultBlockState();
        position = new Rect2i(x, y, 0, 0);
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
    public Rect2i getArea() {
        return position;
    }

    @Override
    public void drawWidget(PoseStack pose, double mouseX, double mouseY) {
        GuiGraphics guiGraphics = new GuiGraphics(pose);
        PoseStack poseStack = guiGraphics.pose();
        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();

        poseStack.translate(1, 1, 0);
        slotDrawable.draw(guiGraphics.pose());
        poseStack.translate(-1, -1, 0);

        poseStack.pushPose();
        poseStack.translate(rect.x(), rect.y(), 0);
        poseStack.translate(15.5, 13.5, 300);
        poseStack.scale(9, -9, 9);
        poseStack.mulPose(Vector3f.XP.rotationDegrees(30f));
        poseStack.mulPose(Vector3f.YP.rotationDegrees(225f));
        blockRenderer.renderSingleBlock(blockState, poseStack, guiGraphics.bufferSource(), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        guiGraphics.bufferSource().endBatch();
        poseStack.popPose();
    }

    public void getTooltip(ITooltipBuilder tooltip, double mouseX, double mouseY) {
        if (slotDrawable.isMouseOver(mouseX, mouseY)) {
            tooltip.add(Component.translatable(blockState.getBlock().getDescriptionId()));
        }
    }
}
