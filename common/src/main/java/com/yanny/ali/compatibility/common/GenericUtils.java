package com.yanny.ali.compatibility.common;

import com.yanny.ali.api.Rect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class GenericUtils {
    private static final ResourceLocation TEXTURE_LOC = com.yanny.ali.Utils.modLoc("textures/gui/gui.png");
    private static final int WIDGET_SIZE = 36;
    private static final int DOTS_WIDTH = Minecraft.getInstance().font.width("...");

    public static void renderEntity(Entity entity, Rect bounds, int fullWidth, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (entity instanceof LivingEntity livingEntity) {
            guiGraphics.pose().pushMatrix();
            guiGraphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    TEXTURE_LOC,
                    bounds.x(),
                    bounds.y(),
                    0,
                    WIDGET_SIZE,
                    bounds.width(),
                    bounds.height(),
                    WIDGET_SIZE,
                    WIDGET_SIZE,
                    256,
                    256
            );

            EntityDimensions dimensions = entity.getType().getDimensions();
            renderEntityInInventoryFollowsMouse(
                    guiGraphics,
                    bounds.x() + 1,
                    bounds.y() + 1,
                    bounds.right() - 1,
                    bounds.bottom() - 1,
                    (int) (Math.min(20 / dimensions.height(), 20 / dimensions.width())),
                    0.0625F,
                    mouseX,
                    mouseY,
                    livingEntity
            );

            guiGraphics.pose().popMatrix();
        }
    }

    @NotNull
    public static Component ellipsis(String text, String fallback, int maxWidth) {
        Font font = Minecraft.getInstance().font;

        text = Language.getInstance().getOrDefault(text, fallback);

        if (font.width(text) > maxWidth) {
            int index = 20;

            while (font.width(text.substring(0, index + 1) + DOTS_WIDTH) <= maxWidth) {
                index += 1;
            }

            return Component.literal(text.substring(0, index) + "...");
        }

        return Component.literal(text);
    }

    public static void renderEntityInInventoryFollowsMouse(GuiGraphics guiGraphics, int left, int top, int right, int bottom,
                                                           int size, float scale, float mouseX, float mouseY, LivingEntity entity) {
        float hCenter = (float)(left + right) / 2.0F;
        float vCenter = (float)(top + bottom) / 2.0F;
        float xRotation = (float)Math.atan((hCenter - mouseX) / 40.0F);
        float yRotation = (float)Math.atan((vCenter - mouseY) / 40.0F);
        float yBodyRot = entity.yBodyRot;
        float entityYRot = entity.getYRot();
        float entityXRot = entity.getXRot();
        float yHeadRotO = entity.yHeadRotO;
        float yHeadRot = entity.yHeadRot;
        Quaternionf rotateZ = (new Quaternionf()).rotateZ((float)Math.PI);
        Quaternionf rotateX = (new Quaternionf()).rotateX(yRotation * 20.0F * ((float)Math.PI / 180F));

        rotateZ.mul(rotateX);
        guiGraphics.enableScissor(left, top, right, bottom);
        entity.yBodyRot = 180.0F + xRotation * 20.0F;
        entity.setYRot(180.0F + xRotation * 40.0F);
        entity.setXRot(-yRotation * 20.0F);
        entity.yHeadRot = entity.getYRot();
        entity.yHeadRotO = entity.getYRot();

        float entityScale = entity.getScale();
        Vector3f $$22 = new Vector3f(0.0F, entity.getBbHeight() / 2.0F + scale * entityScale, 0.0F);
        float $$23 = (float)size / entityScale;
        int x = (int) guiGraphics.pose().m20();
        int y = (int) guiGraphics.pose().m21();
        InventoryScreen.renderEntityInInventory(guiGraphics, left + x, top + y, right + x, bottom + y, $$23, $$22, rotateZ, rotateX, entity);

        entity.yBodyRot = yBodyRot;
        entity.setYRot(entityYRot);
        entity.setXRot(entityXRot);
        entity.yHeadRotO = yHeadRotO;
        entity.yHeadRot = yHeadRot;
        guiGraphics.disableScissor();
    }
}
