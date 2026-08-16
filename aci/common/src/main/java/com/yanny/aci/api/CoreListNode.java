package com.yanny.aci.api;

import com.mojang.logging.LogUtils;
import com.yanny.aci.tooltip.TooltipContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public abstract class CoreListNode<
        TServerUtils extends ICoreServerUtils<?>,
        TDataNode    extends ICoreDataNode<TServerUtils>,
        TClientUtils extends ICoreClientUtils<TDataNode, ?, TClientUtils>
        >
        implements ICoreDataNode<TServerUtils> {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Nullable
    private List<TDataNode> nodes;

    public CoreListNode() {
    }

    public CoreListNode(TClientUtils utils, RegistryFriendlyByteBuf buf) {
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

    /**
     * Whether this node only makes sense with every child present. A villager trade, for example, must not be shown
     * with one of its two inputs missing - the remaining half would read as a different, cheaper trade. Such a node
     * is dropped by {@link #prune} as soon as a single child is rejected, instead of keeping the remainder.
     */
    protected boolean requiresAllChildren() {
        return false;
    }

    /**
     * Recursively drops leaf nodes rejected by {@code keep}, then drops every list node left without children (or,
     * for {@link #requiresAllChildren()} nodes, left incomplete).
     * <p>
     * Unlike {@link #optimizeList()} - which runs server-side on a freshly built, mutable tree - this is meant for
     * the decoded client-side tree, whose child lists are unmodifiable. It therefore replaces the list rather than
     * mutating it in place.
     *
     * @param keep tested against leaf nodes only; list nodes are kept exactly when something survives beneath them
     * @return true if this node itself ended up empty and the caller should drop it
     */
    public boolean prune(Predicate<ICoreDataNode<?>> keep) {
        if (nodes == null || nodes.isEmpty()) {
            nodes = null;
            return true;
        }

        List<TDataNode> kept = new ArrayList<>(nodes.size());

        for (TDataNode node : nodes) {
            if (node instanceof CoreListNode<?, ?, ?> listNode) {
                if (!listNode.prune(keep)) {
                    kept.add(node);
                }
            } else if (keep.test(node)) {
                kept.add(node);
            }
        }

        if (kept.isEmpty() || (requiresAllChildren() && kept.size() != nodes.size())) {
            nodes = null;
            return true;
        }

        nodes = Collections.unmodifiableList(kept);
        return false;
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

    // must not touch TooltipContext - it is set once per top-level entry by NetworkUtils and stays ambient for the siblings
    @Override
    public final void encode(TServerUtils utils, RegistryFriendlyByteBuf buf) {
        List<TDataNode> nodes = nodes();
        int countIndex = buf.writerIndex();
        int successfulNodes = 0;

        buf.writeInt(nodes.size());

        for (TDataNode node : nodes) {
            int startOfNode = buf.writerIndex();

            try {
                buf.writeResourceLocation(node.getId());
                node.encode(utils, buf);
                successfulNodes++;
            } catch (Throwable e) {
                buf.writerIndex(startOfNode);
                LOGGER.warn("[{}] Failed to write child node {} of parent {} (in {})",
                        getId().getNamespace(), node.getId(), getId(), TooltipContext.get(), e);
            }
        }

        if (successfulNodes != nodes.size()) {
            LOGGER.warn("[{}] Dropped {} of {} child node(s) of {} (in {}) while encoding",
                    getId().getNamespace(), nodes.size() - successfulNodes, nodes.size(), getId(), TooltipContext.get());

            int endIndex = buf.writerIndex();

            buf.writerIndex(countIndex);
            buf.writeInt(successfulNodes);
            buf.writerIndex(endIndex);
        }

        encodeNode(utils, buf);
    }

    public abstract void encodeNode(TServerUtils utils, RegistryFriendlyByteBuf buf);
}
