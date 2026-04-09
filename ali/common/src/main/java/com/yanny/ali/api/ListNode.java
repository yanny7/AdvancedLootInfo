package com.yanny.ali.api;

import com.mojang.logging.LogUtils;
import com.yanny.ali.manager.PluginManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class ListNode implements IDataNode {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Nullable
    private List<IDataNode> nodes;

    public ListNode() {
    }

    public ListNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        int count = buf.readInt();

        if (count == 0) {
            nodes = Collections.emptyList();
        } else if (count == 1) {
            nodes = Collections.singletonList(utils.getDataNodeFactory(buf.readIdentifier()).create(utils, buf));
        } else {
            nodes = new ArrayList<>(count);

            for (int i = 0; i < count; i++) {
                nodes.add(utils.getDataNodeFactory(buf.readIdentifier()).create(utils, buf));
            }

            Collections.sort(nodes);
            this.nodes = Collections.unmodifiableList(nodes);
        }
    }

    public List<IDataNode> nodes() {
        return Objects.requireNonNullElse(nodes, Collections.emptyList());
    }

    public void addChildren(IDataNode node) {
        if (nodes == null) {
            nodes = new ArrayList<>();
        }

        nodes.add(node);
    }

    public void optimizeList() {
        if (nodes == null || nodes.isEmpty()) {
            return;
        }

        for (IDataNode node : nodes) {
            if (node instanceof ListNode listNode) {
                listNode.optimizeList();
            }
        }

        nodes.removeIf(node -> {
            if (node instanceof ListNode listNode) {
                return listNode.nodes().isEmpty();
            }

            return false;
        });

        if (nodes.isEmpty()) {
            nodes = null;
        }
    }

    public abstract void encodeNode(IServerUtils utils, RegistryFriendlyByteBuf buf);

    @Override
    public final void encode(IServerUtils utils, RegistryFriendlyByteBuf buf) {
        List<IDataNode> nodes = nodes();
        int countIndex = buf.writerIndex();
        int successfulNodes = 0;

        buf.writeInt(nodes.size());

        for (IDataNode node : nodes) {
            int startOfNode = buf.writerIndex();

            try {
                buf.writeIdentifier(node.getId());
                node.encode(utils, buf);
                successfulNodes++;
            } catch (Throwable e) {
                buf.writerIndex(startOfNode);
                LOGGER.warn("Failed to write node in {}", PluginManager.SERVER_REGISTRY.getCurrentLootTable(), e);
            }
        }

        if (successfulNodes != nodes.size()) {
            int endIndex = buf.writerIndex();

            buf.writerIndex(countIndex);
            buf.writeInt(successfulNodes);
            buf.writerIndex(endIndex);
        }

        encodeNode(utils, buf);
    }
}
