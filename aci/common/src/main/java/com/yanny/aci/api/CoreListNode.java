package com.yanny.aci.api;

import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class CoreListNode
        <
                SU extends ICoreServerUtils,
                TN extends ICoreTooltipNode<SU>,
                DN extends ICoreDataNode<SU, TN>,
                CU extends ICoreClientUtils<SU, TN, DN, CU, WU>,
                WU extends ICoreWidgetUtils<SU, TN, DN>
        >
        implements ICoreDataNode<SU, TN> {
    @Nullable
    private List<DN> nodes;

    public CoreListNode() {
    }

    public CoreListNode(CU utils, FriendlyByteBuf buf) {
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
    public List<DN> nodes() {
        return Objects.requireNonNullElse(nodes, Collections.emptyList());
    }

    public void addChildren(DN node) {
        if (nodes == null) {
            nodes = new ArrayList<>();
        }

        nodes.add(node);
    }

    public void optimizeList() {
        if (nodes == null || nodes.isEmpty()) {
            return;
        }

        for (DN node : nodes) {
            if (node instanceof CoreListNode<?, ?, ?, ?, ?> listNode) {
                listNode.optimizeList();
            }
        }

        nodes.removeIf(node -> {
            if (node instanceof CoreListNode<?, ?, ?, ?, ?> listNode) {
                return listNode.nodes().isEmpty();
            }

            return false;
        });

        if (nodes.isEmpty()) {
            nodes = null;
        }
    }

    public abstract void encodeNode(SU utils, FriendlyByteBuf buf);
}
