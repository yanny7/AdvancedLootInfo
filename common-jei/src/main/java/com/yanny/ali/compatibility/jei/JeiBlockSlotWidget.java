package com.yanny.ali.compatibility.jei;

import com.yanny.ali.api.Rect;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.gui.widgets.ISlottedRecipeWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2fStack;

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
        Matrix3x2fStack poseStack = guiGraphics.pose();
        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();

        poseStack.pushMatrix();
        slotDrawable.draw(guiGraphics);

        if (block.asItem() == Items.AIR) {
            poseStack.translate(rect.x(), rect.y());

            if (isPlant) {
                poseStack.translate(18, 10);
                poseStack.scale(9, -9);
//                poseStack.mulPose(Axis.XP.rotationDegrees(30f)); FIXME - PIP
//                poseStack.mulPose(Axis.YP.rotationDegrees(225f));
//                guiGraphics.drawSpecial((bufferSource) -> blockRenderer.renderSingleBlock(blockState, poseStack, bufferSource, 15728880, OverlayTexture.NO_OVERLAY));
//
//                BlockState base;
//                BlockState farmland = Blocks.FARMLAND.defaultBlockState();
//
//                if (block instanceof MixinVegetationBlock bushBlock && bushBlock.invokeMayPlaceOn(farmland, level, BlockPos.ZERO)) {
//                    base = farmland;
//                } else {
//                    base = Blocks.GRASS_BLOCK.defaultBlockState();
//                }
//
//                poseStack.translate(0, -1);
//                blockRenderer.renderSingleBlock(base, poseStack, guiGraphics, 15728880, OverlayTexture.NO_OVERLAY);
            } else {
                poseStack.translate(25.5F, 21F);
                poseStack.scale(18, -18);
//                poseStack.mulPose(Axis.XP.rotationDegrees(30f));
//                poseStack.mulPose(Axis.YP.rotationDegrees(225f));
//                guiGraphics.drawSpecial((bufferSource) -> blockRenderer.renderSingleBlock(blockState, poseStack, bufferSource, 15728880, OverlayTexture.NO_OVERLAY));
                poseStack.translate(0, -1);
            }
        }

        poseStack.popMatrix();
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
