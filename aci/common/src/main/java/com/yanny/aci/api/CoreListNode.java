package com.yanny.aci.api;

import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class CoreListNode<
        TServerUtils extends ICoreServerUtils<?>,
        TDataNode    extends ICoreDataNode<TServerUtils>,
        TClientUtils extends ICoreClientUtils<TDataNode, ?, TClientUtils>
        >
        implements ICoreDataNode<TServerUtils> {
    @Nullable
    private List<TDataNode> nodes;

    public CoreListNode() {
    }

    public CoreListNode(TClientUtils utils, FriendlyByteBuf buf) {
        int count = buf.readInt();

        if (count == 0) {
            nodes = Collections.emptyList();
        } else if (count == 1) {
            nodes = Collections.singletonList(utils.getDataNodeFactory(buf.readResourceLocation()).apply(utils, buf));
        } else {
            nodes = new ArrayList<>(count);

            for (int i = 0; i < count; i++) {
                nodes.add(utils.getDataNodeFactory(buf.readResourceLocation()).apply(utils, buf));
            }

            Collections.sort(nodes);
            this.nodes = Collections.unmodifiableList(nodes);
        }
    }

    @NotNull
    public List<TDataNode> nodes() {
        return Objects.requireNonNullElse(nodes, Collections.emptyList());
    }

    public void addChildren(TDataNode node) {
        if (nodes == null) {
            nodes = new ArrayList<>();
        }

        nodes.add(node);
    }

    public void optimizeList() {
        if (nodes == null || nodes.isEmpty()) {
            return;
        }

        for (TDataNode node : nodes) {
            if (node instanceof CoreListNode<?, ?, ?> listNode) {
                listNode.optimizeList();
            }
        }

        nodes.removeIf(node -> {
            if (node instanceof CoreListNode<?, ?, ?> listNode) {
                return listNode.nodes().isEmpty();
            }

            return false;
        });

        if (nodes.isEmpty()) {
            nodes = null;
        }
    }

    public abstract void encodeNode(TServerUtils utils, FriendlyByteBuf buf);
}
