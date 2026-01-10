package com.yanny.ali.api;

import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class ListNode implements IDataNode {
    @Nullable
    private List<IDataNode> nodes;

    public ListNode() {
    }

    public ListNode(IClientUtils utils, FriendlyByteBuf buf) {
        int count = buf.readInt();

        if (count == 0) {
            nodes = Collections.emptyList();
        } else if (count == 1) {
            nodes = Collections.singletonList(utils.getDataNodeFactory(buf.readResourceLocation()).create(utils, buf));
        } else {
            nodes = new ArrayList<>(count);

            for (int i = 0; i < count; i++) {
                nodes.add(utils.getDataNodeFactory(buf.readResourceLocation()).create(utils, buf));
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

    public abstract void encodeNode(IServerUtils utils, FriendlyByteBuf buf);

    @Override
    public final void encode(IServerUtils utils, FriendlyByteBuf buf) {
        List<IDataNode> nodes = nodes();

        buf.writeInt(nodes.size());

        for (IDataNode node : nodes) {
            buf.writeResourceLocation(node.getId());
            node.encode(utils, buf);
        }

        encodeNode(utils, buf);
    }
}
