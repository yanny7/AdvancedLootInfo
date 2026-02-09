package com.yanny.ali.jei.compatibility.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.yanny.ali.api.Rect;
import com.yanny.ali.mixin.MixinVegetationBlock;
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
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class JeiBlockSlotWidget implements ISlottedRecipeWidget {
    private final BlockState blockState;
    private final Block block;
    private final boolean isPlant;
    private final ScreenPosition position;
    private final Rect rect;
    private final ClientLevel level;
    private final IRecipeSlotDrawable slotDrawable;

    public JeiBlockSlotWidget(IRecipeSlotDrawable slotDrawable, Block block, int x, int y) {
        this.slotDrawable = slotDrawable;
        this.block = block;
        blockState = block.defaultBlockState();
        isPlant = block instanceof VegetationBlock;
        position = new ScreenPosition(x, y);
        rect = new Rect(x - 4, y - 4, 24, 24);
        level = Minecraft.getInstance().level;
    }

    @NotNull
    @Override
    public Optional<RecipeSlotUnderMouse> getSlotUnderMouse(double v, double v1) {
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

        poseStack.pushPose();
        slotDrawable.draw(guiGraphics);

        if (block.asItem() == Items.AIR) {
            poseStack.translate(rect.x(), rect.y(), 0);

            if (isPlant) {
                poseStack.translate(18, 10, 100);
                poseStack.scale(9, -9, 9);
                poseStack.mulPose(Axis.XP.rotationDegrees(30f));
                poseStack.mulPose(Axis.YP.rotationDegrees(225f));
                guiGraphics.drawSpecial((bufferSource) -> blockRenderer.renderSingleBlock(blockState, poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY));

                BlockState base;
                BlockState farmland = Blocks.FARMLAND.defaultBlockState();

                if (block instanceof MixinVegetationBlock vegetationBlock && vegetationBlock.invokeMayPlaceOn(farmland, level, BlockPos.ZERO)) {
                    base = farmland;
                } else {
                    base = Blocks.GRASS_BLOCK.defaultBlockState();
                }

                poseStack.translate(0, -1, 0);
                guiGraphics.drawSpecial((bufferSource) -> blockRenderer.renderSingleBlock(base, poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY));
            } else {
                poseStack.translate(25.5, 21, 100);
                poseStack.scale(18, -18, 18);
                poseStack.mulPose(Axis.XP.rotationDegrees(30f));
                poseStack.mulPose(Axis.YP.rotationDegrees(225f));
                guiGraphics.drawSpecial((bufferSource) -> blockRenderer.renderSingleBlock(blockState, poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY));
                poseStack.translate(0, -1, 0);
            }
        }

        poseStack.popPose();
    }

    public void getTooltip(ITooltipBuilder tooltip, double mouseX, double mouseY) {
        if (slotDrawable.isMouseOver(mouseX, mouseY)) {
            if (isPlant) {
                tooltip.add(block.getName());
            } else {
                slotDrawable.getTooltip(tooltip);
            }
        }
    }
}
