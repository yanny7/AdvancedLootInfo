package com.yanny.ali.network;

import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.plugin.common.trades.TradeNode;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class LootDataReader {
    @NotNull
    public static FriendlyByteBuf decompress(byte[] fullCompressedData) {
        ByteArrayInputStream bis = new ByteArrayInputStream(fullCompressedData);
        ByteBuf decompressedBuf = Unpooled.buffer();

        try (GZIPInputStream gzip = new GZIPInputStream(bis)) {
            decompressedBuf.writeBytes(gzip.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new FriendlyByteBuf(decompressedBuf);
    }

    public static void decode(IClientUtils utils, FriendlyByteBuf buf, Map<ResourceLocation, IDataNode> lootData, Map<ResourceLocation, IDataNode> tradeData) {
        utils.getTooltipCache().decode(utils, buf);
        readLootData(utils, buf, lootData);
        readTradeData(utils, buf, tradeData);
    }

    private static void readLootData(IClientUtils utils, FriendlyByteBuf readerBuf, Map<ResourceLocation, IDataNode> lootData) {
        int lootDataCount = readerBuf.readInt();

        for (int i = 0; i < lootDataCount; i++) {
            ResourceLocation location = readerBuf.readResourceLocation();
            ResourceLocation id = readerBuf.readResourceLocation();

            lootData.put(location, utils.getDataNodeFactory(id).apply(utils, readerBuf));
        }
    }

    private static void readTradeData(IClientUtils utils, FriendlyByteBuf buf, Map<ResourceLocation, IDataNode> tradeData) {
        int tradeDataCount = buf.readInt();

        for (int i = 0; i < tradeDataCount; i++) {
            ResourceLocation location = buf.readResourceLocation();

            tradeData.put(location, utils.getDataNodeFactory(TradeNode.ID).apply(utils, buf));
        }
    }
}
