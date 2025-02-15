package com.yanny.ali.compatibility.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.yanny.ali.api.Rect;
import com.yanny.ali.mixin.MixinBushBlock;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.gui.widgets.ISlottedRecipeWidget;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.common.IPlantable;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class JeiBlockSlotWidget implements ISlottedRecipeWidget {
    private final BlockState blockState;
    private final Block block;
    private final boolean isPlant;
    private final ScreenPosition position;
    private final Rect rect;
    private final IGuiHelper guiHelper;

    public JeiBlockSlotWidget(IGuiHelper guiHelper, Block block, int x, int y) {
        this.guiHelper = guiHelper;
        this.block = block;
        blockState = block.defaultBlockState();
        isPlant = block instanceof IPlantable;
        position = new ScreenPosition(x, y);
        rect = new Rect(x - 4, y - 4, 24, 24);
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
        guiHelper.getOutputSlot().draw(guiGraphics, -5, -5);

        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        PoseStack poseStack = guiGraphics.pose();

        poseStack.pushPose();

        if (isPlant) {
            poseStack.translate(14, 8, 100);
            poseStack.scale(9, -9, 9);
            poseStack.mulPose(Axis.XP.rotationDegrees(30f));
            poseStack.mulPose(Axis.YP.rotationDegrees(225f));
            blockRenderer.renderSingleBlock(blockState, poseStack, guiGraphics.bufferSource(), 15728880, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.cutout());

            BlockState base;
            BlockState farmland = Blocks.FARMLAND.defaultBlockState();

            if (block instanceof MixinBushBlock bushBlock && bushBlock.invokeMayPlaceOn(farmland, Minecraft.getInstance().level, BlockPos.ZERO)) {
                base = farmland;
            } else {
                base = Blocks.GRASS_BLOCK.defaultBlockState();
            }

            poseStack.translate(0, -1, 0);
            blockRenderer.renderSingleBlock(base, poseStack, guiGraphics.bufferSource(), 15728880, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.cutout());
        } else {
            poseStack.translate(25.5, 21, 100);
            poseStack.scale(18, -18, 18);
            poseStack.mulPose(Axis.XP.rotationDegrees(30f));
            poseStack.mulPose(Axis.YP.rotationDegrees(225f));
            blockRenderer.renderSingleBlock(blockState, poseStack, guiGraphics.bufferSource(), 15728880, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.cutout());
            poseStack.translate(0, -1, 0);
        }

        poseStack.popPose();
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, double mouseX, double mouseY) {
        if (rect.contains((int) mouseX + position.x(), (int) mouseY + position.y())) {
            tooltip.add(Component.translatable(block.getDescriptionId()));
        }
    }
}
