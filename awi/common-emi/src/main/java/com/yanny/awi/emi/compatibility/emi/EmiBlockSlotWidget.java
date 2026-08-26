package com.yanny.awi.emi.compatibility.emi;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.yanny.aci.tooltip.CoreTooltipUtils;
import com.yanny.awi.api.IDataNode;
import com.yanny.awi.plugin.client.TooltipUtils;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

import java.util.List;

public class EmiBlockSlotWidget extends SlotWidget {
    private final BlockState blockState;
    private final IDataNode entry;

    public EmiBlockSlotWidget(IDataNode entry, Block block, int x, int y) {
        super(EmiStack.of(block), x, y);
        this.blockState = block.defaultBlockState();
        this.entry = entry;
    }

    @Override
    protected void addSlotTooltip(List<ClientTooltipComponent> list) {
        CoreTooltipUtils.toComponents(entry.getTooltip(), 0, Minecraft.getInstance().options.advancedItemTooltips, TooltipUtils.getStyle())
                .forEach((c) -> list.add(ClientTooltipComponent.create(c.getVisualOrderText())));
        super.addSlotTooltip(list);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        drawBackground(guiGraphics, mouseX, mouseY, delta);
        drawOverlay(guiGraphics, mouseX, mouseY, delta);

        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        PoseStack poseStack = guiGraphics.pose();

        Vector3f light0 = new Vector3f(0.6F, -1.0F, 0.8F).normalize();
        Vector3f light1 = new Vector3f(-0.6F, 1.0F, -0.8F).normalize();
        RenderSystem.setShaderLights(light0, light1);

        poseStack.pushPose();
        poseStack.translate(x, y, 0);
        poseStack.translate(15.5, 13.5, 300);
        poseStack.scale(9, -9, 9);
        poseStack.mulPose(Axis.XP.rotationDegrees(30f));
        poseStack.mulPose(Axis.YP.rotationDegrees(225f));
        blockRenderer.renderSingleBlock(blockState, poseStack, guiGraphics.bufferSource(), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        guiGraphics.bufferSource().endBatch();
        poseStack.popPose();
    }

    @Override
    public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
        if (blockState.getBlock().asItem() == Items.AIR) {
            return List.of(ClientTooltipComponent.create(Component.translatable(blockState.getBlock().getDescriptionId()).getVisualOrderText()));
        }

        return super.getTooltip(mouseX, mouseY);
    }
}
