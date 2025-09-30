package com.yanny.ali.compatibility.common;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.Rect;
import com.yanny.ali.manager.AliClientRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GenericUtils {
    private static final ResourceLocation TEXTURE_LOC = com.yanny.ali.Utils.modLoc("textures/gui/gui.png");
    private static final int WIDGET_SIZE = 36;
    private static final int DOTS_WIDTH = Minecraft.getInstance().font.width("...");

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
                    RenderType::guiTextured,
                    TEXTURE_LOC,
                    bounds.x(),
                    bounds.y(),
                    0,
                    36,
                    bounds.width(),
                    bounds.height(),
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
                    mouseX - fullWidth / 2f,
                    mouseY,
                    livingEntity
            );

            guiGraphics.disableScissor();
            guiGraphics.pose().popPose();
        }
    }

    @NotNull
    public static Component ellipsis(String text, String fallback, int maxWidth) {
        Font font = Minecraft.getInstance().font;

        text = Language.getInstance().getOrDefault(text, getFallbackText(fallback));

        if (font.width(text) > maxWidth) {
            int index = 20;

            while (font.width(text.substring(0, index + 1) + DOTS_WIDTH) <= maxWidth) {
                index += 1;
            }

            return Component.literal(text.substring(0, index) + "...");
        }

        return Component.literal(text);
    }

    public static void processData(ClientLevel level, AliClientRegistry clientRegistry,
                                   Map<ResourceLocation, IDataNode> lootData, Map<ResourceLocation, IDataNode> tradeData,
                                   QuadConsumer<IDataNode, ResourceLocation, Block, List<ItemStack>> blockConsumer,
                                   QuadConsumer<IDataNode, ResourceLocation, EntityType<?>, List<ItemStack>> entityConsumer,
                                   TriConsumer<IDataNode, ResourceLocation, List<ItemStack>> gameplayConsumer,
                                   QuadConsumer<IDataNode, ResourceLocation, List<ItemStack>, List<ItemStack>> traderConsumer,
                                   QuadConsumer<IDataNode, ResourceLocation, List<ItemStack>, List<ItemStack>> wanderingTraderConsumer) {
        for (Block block : BuiltInRegistries.BLOCK) {
            block.getLootTable().ifPresent((resourceKey -> {
                ResourceLocation location = resourceKey.location();
                IDataNode lootEntry = lootData.get(location);

                if (lootEntry != null) {
                    List<ItemStack> outputs = clientRegistry.getLootItems(location);

                    blockConsumer.accept(lootEntry, location, block, outputs);
                    lootData.remove(location);
                }
            }));
        }

        for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
            List<Entity> entityList = clientRegistry.createEntities(entityType, level);

            for (Entity entity : entityList) {
                if (entity instanceof Mob mob) {
                    mob.getLootTable().ifPresent((resourceKey) -> {
                        ResourceLocation location = resourceKey.location();
                        IDataNode lootEntry = lootData.get(location);

                        if (lootEntry != null) {
                            List<ItemStack> outputs = clientRegistry.getLootItems(location);

                            if (outputs != null) {
                                entityConsumer.accept(lootEntry, location, entityType, outputs);
                            }
                        }

                        lootData.remove(location);
                    });
                }
            }
        }

        for (Map.Entry<ResourceLocation, IDataNode> entry : lootData.entrySet()) {
            ResourceLocation location = entry.getKey();
            List<ItemStack> outputs = clientRegistry.getLootItems(location);

            if (outputs != null) {
                gameplayConsumer.accept(entry.getValue(), entry.getKey(), outputs);
            }
        }

        lootData.clear();

        List<Map.Entry<ResourceKey<VillagerProfession>, VillagerProfession>> entries = BuiltInRegistries.VILLAGER_PROFESSION.entrySet()
                .stream()
                .sorted(Comparator.comparing(a -> a.getKey().location().getPath()))
                .toList();

        for (Map.Entry<ResourceKey<VillagerProfession>, VillagerProfession> entry : entries) {
            ResourceLocation location = entry.getKey().location();
            IDataNode tradeEntry = tradeData.get(location);

            if (tradeEntry != null) {
                List<ItemStack> inputs = clientRegistry.getTradeInputItems(location).stream().map(Item::getDefaultInstance).toList();
                List<ItemStack> outputs = clientRegistry.getTradeOutputItems(location).stream().map(Item::getDefaultInstance).toList();

                traderConsumer.accept(tradeEntry, location, inputs, outputs);
                tradeData.remove(location);
            }
        }

        for (Map.Entry<ResourceLocation, IDataNode> entry : tradeData.entrySet()) {
            ResourceLocation location = entry.getKey();
            IDataNode tradeEntry = tradeData.get(location);

            if (tradeEntry != null) {
                List<ItemStack> inputs = clientRegistry.getTradeInputItems(location).stream().map(Item::getDefaultInstance).toList();
                List<ItemStack> outputs = clientRegistry.getTradeOutputItems(location).stream().map(Item::getDefaultInstance).toList();

                wanderingTraderConsumer.accept(tradeEntry, location, inputs, outputs);
            }
        }

        tradeData.clear();
    }

    // split table path and make uppercased text
    private static String getFallbackText(String fallback) {
        List<String> pathSegments = Pattern.compile("/").splitAsStream(fallback).filter(s -> !s.isEmpty()).collect(Collectors.toList());

        Collections.reverse(pathSegments);

        return pathSegments.stream()
                .flatMap(segment -> Arrays.stream(segment.split("_")))
                .filter(s -> !s.isEmpty())
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .collect(Collectors.joining(" "));
    }
}
