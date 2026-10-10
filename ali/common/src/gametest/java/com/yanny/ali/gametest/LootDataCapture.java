package com.yanny.ali.gametest;

import com.yanny.aci.api.ICoreDataNode;
import com.yanny.aci.gametest.GoldenRenderer;
import com.yanny.aci.number.NumberFormatter;
import com.yanny.aci.tooltip.NumberOptions;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IItemNode;
import com.yanny.ali.configuration.AliConfig;
import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.network.AbstractServer;
import com.yanny.ali.network.LootDataChunkMessage;
import com.yanny.ali.network.LootDataReader;
import com.yanny.ali.plugin.client.TooltipUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class LootDataCapture {
    @NotNull
    public static Map<String, String> capture(AbstractServer aliServer, AliConfig config) {
        IClientUtils utils = PluginManager.getInstance().clientRegistry;
        FriendlyByteBuf buf = LootDataReader.decompress(readChunks(aliServer));
        Map<ResourceLocation, IDataNode> lootData = new HashMap<>();
        Map<ResourceLocation, IDataNode> tradeData = new HashMap<>();

        try {
            LootDataReader.decode(utils, buf, lootData, tradeData);

            if (buf.isReadable()) {
                throw new GameTestAssertException(buf.readableBytes() + " byte(s) left unread after decoding");
            }
        } finally {
            buf.release();
        }

        Map<String, IDataNode> pages = new TreeMap<>();

        lootData.forEach((location, node) -> pages.put("loot/" + location.getNamespace() + "/" + location.getPath() + ".txt", node));
        tradeData.forEach((location, node) -> pages.put("trade/" + location.getNamespace() + "/" + location.getPath() + ".txt", node));

        Map<String, String> files = new TreeMap<>();

        pages.forEach((key, node) -> {
            files.put("normal/" + key, renderer(false, config).render(node));
            files.put("advanced/" + key, renderer(true, config).render(node));
        });

        return files;
    }

    private static byte[] readChunks(AbstractServer aliServer) {
        List<LootDataChunkMessage> chunks = aliServer.getChunks();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        if (chunks.isEmpty()) {
            throw new GameTestAssertException("Server produced no loot data");
        }

        for (LootDataChunkMessage chunk : chunks) {
            bos.writeBytes(chunk.data());
        }

        return bos.toByteArray();
    }

    @NotNull
    private static GoldenRenderer renderer(boolean isAdvanced, AliConfig config) {
        return new GoldenRenderer(isAdvanced, new NumberOptions(isAdvanced, config.showCharts, Locale.ROOT)) {
            @Override
            protected String describe(ICoreDataNode<?> node) {
                return node instanceof IItemNode itemNode ? itemToString(itemNode) : null;
            }
        };
    }

    @NotNull
    private static String itemToString(IItemNode node) {
        String count = " x" + NumberFormatter.slot(TooltipUtils.getSlotCount(node));

        return node.getItem().map((stack) -> stackToString(stack, count), (tag) -> "#" + tag.location() + count);
    }

    @NotNull
    private static String stackToString(ItemStack stack, String count) {
        String text = BuiltInRegistries.ITEM.getKey(stack.getItem()) + (stack.isEmpty() ? " x0" : count);

        return stack.hasTag() ? text + " " + stack.getTag() : text;
    }
}
