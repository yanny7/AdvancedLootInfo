package com.yanny.ali.compatibility.emi;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.yanny.ali.mixin.MixinBushBlock;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.common.IPlantable;

import java.util.List;

public class EmiBlockSlotWidget extends SlotWidget {
    private final BlockState blockState;
    private final Block block;
    private final boolean isPlant;

    public EmiBlockSlotWidget(Block block, int x, int y) {
        super(EmiStack.of(block), x, y);
        this.block = block;
        blockState = block.defaultBlockState();
        isPlant = block instanceof IPlantable;
        large(true);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        drawBackground(guiGraphics, mouseX, mouseY, delta);
        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        PoseStack poseStack = guiGraphics.pose();

        poseStack.pushPose();
        poseStack.translate(x, y, 0);

        if (isPlant) {
            poseStack.translate(19, 12.5, 100);
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
        drawOverlay(guiGraphics, mouseX, mouseY, delta);
    }

    @Override
    public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
        if (block.asItem() == Items.AIR) {
            return List.of(ClientTooltipComponent.create(Component.translatable(block.getDescriptionId()).getVisualOrderText()));
        }

        return super.getTooltip(mouseX, mouseY);
    }
}
