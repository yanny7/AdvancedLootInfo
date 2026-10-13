package com.yanny.ali.network;

import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.plugin.common.trades.TradeNode;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class LootDataReader {
    @NotNull
    public static RegistryFriendlyByteBuf decompress(byte[] fullCompressedData, RegistryAccess registryAccess) {
        ByteArrayInputStream bis = new ByteArrayInputStream(fullCompressedData);
        ByteBuf decompressedBuf = Unpooled.buffer();

        try (GZIPInputStream gzip = new GZIPInputStream(bis)) {
            decompressedBuf.writeBytes(gzip.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new RegistryFriendlyByteBuf(decompressedBuf, registryAccess);
    }

    public static void decode(IClientUtils utils, RegistryFriendlyByteBuf buf, Map<Identifier, IDataNode> lootData, Map<Identifier, IDataNode> tradeData) {
        utils.getTooltipCache().decode(utils, buf);
        readLootData(utils, buf, lootData);
        readTradeData(utils, buf, tradeData);
    }

    private static void readLootData(IClientUtils utils, RegistryFriendlyByteBuf readerBuf, Map<Identifier, IDataNode> lootData) {
        int lootDataCount = readerBuf.readInt();

        for (int i = 0; i < lootDataCount; i++) {
            Identifier location = readerBuf.readIdentifier();
            Identifier id = readerBuf.readIdentifier();

            lootData.put(location, utils.getDataNodeFactory(id).apply(utils, readerBuf));
        }
    }

    private static void readTradeData(IClientUtils utils, RegistryFriendlyByteBuf buf, Map<Identifier, IDataNode> tradeData) {
        int tradeDataCount = buf.readInt();

        for (int i = 0; i < tradeDataCount; i++) {
            Identifier location = buf.readIdentifier();

            tradeData.put(location, utils.getDataNodeFactory(TradeNode.ID).apply(utils, buf));
        }
    }
}
