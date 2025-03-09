package com.yanny.ali.compatibility.common;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.yanny.ali.api.Rect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class GenericUtils {
    private static final ResourceLocation TEXTURE_LOC = com.yanny.ali.Utils.modLoc("textures/gui/gui.png");
    private static final int WIDGET_SIZE = 36;

    public static void renderEntity(Entity entity, Rect bounds, int fullWidth, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        Minecraft minecraft = Minecraft.getInstance();
        Window window = minecraft.getWindow();
        PoseStack poseStack = guiGraphics.pose();

        // Get the model-view matrix (combined) from the PoseStack
        Matrix4f modelViewMatrix = new Matrix4f(poseStack.last().pose());
        // Get the projection matrix
        Matrix4f projectionMatrix = new Matrix4f(RenderSystem.getProjectionMatrix());
        // Combine model-view and projection
        Matrix4f mvpMatrix = projectionMatrix.mul(modelViewMatrix);
        // Define the 3D coordinates of the top-left and bottom-right corners of your element
        // Since it's a 2D element in GUI, Z can be 0.
        Vector4f topLeftWorld = new Vector4f(0, 0, 0, 1);
        // Project to clip space
        Vector4f topLeftClip = mvpMatrix.transform(topLeftWorld);
        // Perspective divide
        Vector4f topLeftNDC = new Vector4f(topLeftClip.x / topLeftClip.w, topLeftClip.y / topLeftClip.w, 0, 1);

        // Convert to screen coordinates (pixels)
        int screenX = Math.round((topLeftNDC.x + 1) / 2f * window.getGuiScaledWidth());
        int screenY = Math.round((1 - topLeftNDC.y) / 2f * window.getGuiScaledHeight());

        if (entity instanceof LivingEntity livingEntity) {
            guiGraphics.pose().pushPose();
            guiGraphics.blit(
                    TEXTURE_LOC,
                    bounds.x(),
                    bounds.y(),
                    bounds.width(),
                    bounds.height(),
                    0,
                    36,
                    36,
                    36,
                    256,
                    256
            );

            guiGraphics.enableScissor(screenX + bounds.x() + 1, screenY + bounds.y() + 1, screenX + bounds.right() - 1, screenY + bounds.bottom() - 1);

            EntityDimensions dimensions = entity.getType().getDimensions();
            InventoryScreen.renderEntityInInventoryFollowsMouse(
                    guiGraphics,
                    -screenX + bounds.x(),
                    -screenY + bounds.y(),
                    screenX + bounds.right(),
                    screenY + bounds.bottom(),
                    (int) (Math.min(20 / dimensions.height(), 20 / dimensions.width())),
                    0.0625F,
                    mouseX,
                    mouseY,
                    livingEntity
            );

            guiGraphics.disableScissor();
            guiGraphics.pose().popPose();
        }
    }
}
