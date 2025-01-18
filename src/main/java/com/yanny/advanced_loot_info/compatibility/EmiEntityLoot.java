package com.yanny.advanced_loot_info.compatibility;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.yanny.advanced_loot_info.Utils;
import com.yanny.advanced_loot_info.network.LootTableEntry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.common.ForgeSpawnEggItem;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.List;

public class EmiEntityLoot extends EmiBaseLoot {
    private final Entity entity;
    private static final ResourceLocation TEXTURE_LOC = Utils.modLoc("textures/gui/gui.png");

    public EmiEntityLoot(EmiRecipeCategory category, ResourceLocation id, Entity entity, LootTableEntry message) {
        super(category, id, message);
        this.entity = entity;

        SpawnEggItem spawnEgg = ForgeSpawnEggItem.fromEntityType(entity.getType());

        if (spawnEgg != null) {
            catalysts = List.of(EmiStack.of(spawnEgg));
        }
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        ClientLevel level = Minecraft.getInstance().level;

        if (level != null) {
            int length = Minecraft.getInstance().font.width(entity.getDisplayName());

            widgetHolder.add(new Widget() {
                private static final int WIDGET_SIZE = 36;
                final Bounds bounds = new Bounds((widgetHolder.getWidth() - WIDGET_SIZE) / 2, 10, WIDGET_SIZE, WIDGET_SIZE);

                @Override
                public Bounds getBounds() {
                    return bounds;
                }

                @Override
                public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
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
                        guiGraphics.blitNineSlicedSized(
                                TEXTURE_LOC,
                                bounds.x(),
                                bounds.y(),
                                bounds.width(),
                                bounds.height(),
                                4,
                                16,
                                16,
                                2,
                                2,
                                256,
                                256
                        );

                        guiGraphics.enableScissor(screenX + bounds.x() + 1, screenY + bounds.y() + 1, screenX + bounds.right() - 1, screenY + bounds.bottom() - 1);

                        EntityDimensions dimensions = EmiEntityLoot.this.entity.getType().getDimensions();
                        InventoryScreen.renderEntityInInventoryFollowsMouse(
                                guiGraphics,
                                bounds.x() + bounds.width() / 2,
                                bounds.y() + WIDGET_SIZE - 5,
                                (int) (Math.min(20 / dimensions.height, 20 / dimensions.width)),
                                -mouseX + ((float) widgetHolder.getWidth() / 2),
                                -mouseY + WIDGET_SIZE / 2f,
                                livingEntity
                        );

                        guiGraphics.disableScissor();
                        guiGraphics.pose().popPose();
                    }
                }
            });
            addWidgets(widgetHolder, new int[]{0, 48});
            widgetHolder.addText(entity.getDisplayName(), (widgetHolder.getWidth() - length) / 2, 0, 0, false);
        }

        catalysts.forEach((catalyst) -> {
            widgetHolder.addSlot(catalyst, 0, 0);
        });
    }

    @Override
    public int getDisplayHeight() {
        return 72 + getItemsHeight();
    }
}
