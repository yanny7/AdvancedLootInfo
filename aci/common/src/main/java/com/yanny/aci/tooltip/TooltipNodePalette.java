package com.yanny.aci.tooltip;

import com.yanny.aci.CommonLogUtils;
import com.yanny.aci.api.ICoreClientUtils;
import com.yanny.aci.api.ICoreServerUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;

public class TooltipNodePalette {
    private final Logger logger;
    private final String modId;

    private final Map<CacheKey, TooltipNode> pool = new HashMap<>();
    private final List<TooltipNode> idToNode = new ArrayList<>();
    private final Map<TooltipNode, Integer> nodeToId = new IdentityHashMap<>();

    private int hits = 0;
    private int misses = 0;

    public TooltipNodePalette(String modId) {
        this.logger = CommonLogUtils.getLogger(modId);
        this.modId = modId;
    }

    @NotNull
    public String getModId() {
        return modId;
    }

    public TooltipNode getOrCreate(CacheKey key) {
        TooltipNode cached = pool.get(key);

        if (cached != null) {
            hits++;
            return cached;
        }

        TooltipNode newNode = new TooltipNode(key);

        misses++;
        pool.put(key, newNode);
        nodeToId.put(newNode, idToNode.size());
        idToNode.add(newNode);
        return newNode;
    }

    public int getNodeId(TooltipNode node) {
        Integer id = nodeToId.get(node);

        if (id == null) {
            // Writing -1 here produces a payload the client cannot decode (getNodeById(-1) throws), which aborts the
            // whole decode and silently truncates the data - so fail loudly on the server instead.
            throw new IllegalStateException("Tooltip node is not present in the palette (palette size: " + idToNode.size()
                    + "). It was created outside of TooltipNodePalette#getOrCreate or after the palette was encoded/cleared.");
        }

        return id;
    }

    public TooltipNode getNodeById(int id) {
        if (id < 0 || id >= idToNode.size()) {
            throw new IllegalStateException("Tooltip node id " + id + " is out of palette bounds (size: " + idToNode.size()
                    + "). Stream is most likely desynchronized.");
        }

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
        nodeToId.clear();

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
        nodeToId.clear();
        hits = 0;
        misses = 0;
    }

    public void logStatistics() {
        double total = hits + misses;

        logger.info("Node Statistics:");
        logger.info("Total Requests: {}", (int) total);
        logger.info("Hits (Reused):  {} ({})", hits, String.format("%.2f%%", (hits / total) * 100));
        logger.info("Misses (New):   {} ({})", misses, String.format("%.2f%%", (misses / total) * 100));
    }
}
