package com.yanny.aci.tooltip;

import com.mojang.logging.LogUtils;
import com.yanny.aci.api.ICoreClientUtils;
import com.yanny.aci.api.ICoreServerUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TooltipNodePalette {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Map<CacheKey, TooltipNode> pool = new HashMap<>();
    private final List<TooltipNode> idToNode = new ArrayList<>();

    private int hits = 0;
    private int misses = 0;

    public TooltipNode getOrCreate(CacheKey key) {
        if (pool.containsKey(key)) {
            hits++;
            return pool.get(key);
        }

        TooltipNode newNode = new TooltipNode(key);

        misses++;
        pool.put(key, newNode);
        idToNode.add(newNode);
        return newNode;
    }

    public int getNodeId(TooltipNode node) {
        return idToNode.indexOf(node);
    }

    public TooltipNode getNodeById(int id) {
        return idToNode.get(id);
    }

    public void encode(ICoreServerUtils<?> utils, RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(idToNode.size());

        for (TooltipNode node : idToNode) {
            node.encode(utils, buf);
        }
    }

    public void decode(ICoreClientUtils<?, ?, ?> utils, RegistryFriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<RawTooltipNode> rawTooltipNodes = new ArrayList<>(size);

        idToNode.clear();
        pool.clear();

        for (int i = 0; i < size; i++) {
            rawTooltipNodes.add(TooltipNode.decodeRaw(utils, buf));
        }

        for (int i = 0; i < size; i++) {
            RawTooltipNode raw = rawTooltipNodes.get(i);
            List<TooltipNode> nodeChildren = new ArrayList<>(raw.children().size());

            for (Integer id : raw.children()) {
                nodeChildren.add(idToNode.get(id));
            }

            TooltipNode.getOrCreate(utils.getTooltipCache(), raw.key(), raw.values(), raw.componentValue(), raw.flags(), nodeChildren);
        }
    }

    public void clear() {
        idToNode.clear();
        pool.clear();
        hits = 0;
        misses = 0;
    }

    public void logStatistics() {
        double total = hits + misses;

        LOGGER.info("Node Statistics:");
        LOGGER.info("Total Requests: {}", (int) total);
        LOGGER.info("Hits (Reused):  {} ({})", hits, String.format("%.2f%%", (hits / total) * 100));
        LOGGER.info("Misses (New):   {} ({})", misses, String.format("%.2f%%", (misses / total) * 100));
    }
}
