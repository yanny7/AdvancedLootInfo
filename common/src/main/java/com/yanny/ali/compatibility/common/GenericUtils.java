package com.yanny.ali.compatibility.common;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.Rect;
import com.yanny.ali.configuration.AliConfig;
import com.yanny.ali.manager.AliClientRegistry;
import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.plugin.common.nodes.LootTableNode;
import com.yanny.ali.plugin.common.trades.TradeNode;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import oshi.util.tuples.Pair;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

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
                    (int) (Math.min(20 / dimensions.height, 20 / dimensions.width)),
                    0.0625F,
                    mouseX,
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

    public static Pair<Map<ResourceLocation, LootData>, Map<ResourceLocation, TradeData>> decompressLootData(byte[] fullCompressedData) {
        Map<ResourceLocation, LootData> lootData = new HashMap<>();
        Map<ResourceLocation, TradeData> tradeData = new HashMap<>();
        ByteArrayInputStream bis = new ByteArrayInputStream(fullCompressedData);
        ByteBuf decompressedBuf = Unpooled.buffer();

        try (GZIPInputStream gzip = new GZIPInputStream(bis)) {
            decompressedBuf.writeBytes(gzip.readAllBytes());
//            decompressedBuf.writeBytes(gzip, gzip.available());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        FriendlyByteBuf readerBuf = new FriendlyByteBuf(decompressedBuf);

        try {
            IClientUtils utils = PluginManager.CLIENT_REGISTRY;

            lootData.putAll(readerBuf.readCollection(ArrayList::new, (b) -> {
                ResourceLocation location = b.readResourceLocation();
                IDataNode dataNode = utils.getDataNodeFactory(LootTableNode.ID).create(utils, b);
                List<ItemStack> items = b.readCollection(ArrayList::new, FriendlyByteBuf::readItem);
                return new AbstractMap.SimpleEntry<>(location, new LootData(dataNode, items));
            }).stream().collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue)));

            tradeData.putAll(readerBuf.readCollection(ArrayList::new, (b) -> {
                ResourceLocation location = b.readResourceLocation();
                IDataNode dataNode = utils.getDataNodeFactory(TradeNode.ID).create(utils, b);
                List<Item> inputs = b.readCollection(ArrayList::new, FriendlyByteBuf::readResourceLocation).stream().map(BuiltInRegistries.ITEM::get).toList();
                List<Item> outputs = b.readCollection(ArrayList::new, FriendlyByteBuf::readResourceLocation).stream().map(BuiltInRegistries.ITEM::get).toList();
                return new AbstractMap.SimpleEntry<>(location, new TradeData(dataNode, inputs, outputs));
            }).stream().collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue)));
        } finally {
            readerBuf.release();
        }

        return new Pair<>(lootData, tradeData);
    }

    public static void processData(ClientLevel level, AliClientRegistry clientRegistry, AliConfig config, byte[] fullCompressedData,
                                   QuadConsumer<IDataNode, ResourceLocation, Block, List<ItemStack>> blockConsumer,
                                   QuadConsumer<IDataNode, ResourceLocation, EntityType<?>, List<ItemStack>> entityConsumer,
                                   TriConsumer<IDataNode, ResourceLocation, List<ItemStack>> gameplayConsumer,
                                   QuadConsumer<IDataNode, ResourceLocation, List<ItemStack>, List<ItemStack>> traderConsumer,
                                   QuadConsumer<IDataNode, ResourceLocation, List<ItemStack>, List<ItemStack>> wanderingTraderConsumer) {
        Pair<Map<ResourceLocation, LootData>, Map<ResourceLocation, TradeData>> pair = GenericUtils.decompressLootData(fullCompressedData);
        Map<ResourceLocation, LootData> lootData = pair.getA();
        Map<ResourceLocation, TradeData> tradeData = pair.getB();

        for (Block block : BuiltInRegistries.BLOCK) {
            ResourceLocation location = block.getLootTable();

            //noinspection ConstantValue
            if (location != null) {
                LootData data = lootData.get(location);

                if (data != null) {
                    blockConsumer.accept(data.node, location, block, data.items);
                    lootData.remove(location);
                }
            }
        }

        for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
            if (config.disabledEntities.stream().anyMatch((f) -> f.equals(BuiltInRegistries.ENTITY_TYPE.getKey(entityType)))) {
                lootData.remove(entityType.getDefaultLootTable()); // at least remove entity default loot table
                continue;
            }

            List<Entity> entityList = clientRegistry.createEntities(entityType, level);

            for (Entity entity : entityList) {
                if (entity instanceof Mob mob) {
                    ResourceLocation location = mob.getLootTable();

                    //noinspection ConstantValue
                    if (location != null) {
                        LootData data = lootData.get(location);

                        if (data != null) {
                            entityConsumer.accept(data.node, location, entityType, data.items);
                        }

                        lootData.remove(location);
                    }
                }
            }
        }

        for (Map.Entry<ResourceLocation, LootData> entry : lootData.entrySet()) {
            gameplayConsumer.accept(entry.getValue().node, entry.getKey(), entry.getValue().items());
        }

        lootData.clear();

        List<Map.Entry<ResourceKey<VillagerProfession>, VillagerProfession>> entries = BuiltInRegistries.VILLAGER_PROFESSION.entrySet()
                .stream()
                .sorted(Comparator.comparing(a -> a.getKey().location().getPath()))
                .toList();

        for (Map.Entry<ResourceKey<VillagerProfession>, VillagerProfession> entry : entries) {
            ResourceLocation location = entry.getKey().location();
            TradeData tradeEntry = tradeData.get(location);

            if (tradeEntry != null) {
                List<ItemStack> inputs = tradeEntry.inputs.stream().map(Item::getDefaultInstance).toList();
                List<ItemStack> outputs = tradeEntry.outputs.stream().map(Item::getDefaultInstance).toList();

                traderConsumer.accept(tradeEntry.node, location, inputs, outputs);
                tradeData.remove(location);
            }
        }

        for (Map.Entry<ResourceLocation, TradeData> entry : tradeData.entrySet()) {
            ResourceLocation location = entry.getKey();
            TradeData tradeEntry = tradeData.get(location);

            if (tradeEntry != null) {
                List<ItemStack> inputs = tradeEntry.inputs.stream().map(Item::getDefaultInstance).toList();
                List<ItemStack> outputs = tradeEntry.outputs.stream().map(Item::getDefaultInstance).toList();

                wanderingTraderConsumer.accept(tradeEntry.node, location, inputs, outputs);
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

    public record LootData(IDataNode node, List<ItemStack> items) {}

    public record TradeData(IDataNode node, List<Item> inputs, List<Item> outputs) {}
}
