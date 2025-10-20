package com.yanny.ali.api;

import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class ListNode implements IDataNode {
    private final List<IDataNode> nodes;

    public ListNode() {
        nodes = new ArrayList<>();
    }

    public ListNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        int count = buf.readInt();
        List<IDataNode> nodes = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            nodes.add(utils.getNodeFactory(buf.readResourceLocation()).create(utils, buf));
        }

        Collections.sort(nodes);
        this.nodes = Collections.unmodifiableList(nodes);
    }

    public List<IDataNode> nodes() {
        return nodes;
    }

    public void addChildren(IDataNode node) {
        nodes.add(node);
    }

    public abstract void encodeNode(IServerUtils utils, RegistryFriendlyByteBuf buf);

    @Override
    public final void encode(IServerUtils utils, RegistryFriendlyByteBuf buf) {
        buf.writeInt(nodes.size());

        for (IDataNode node : nodes) {
            buf.writeResourceLocation(node.getId());
            node.encode(utils, buf);
        }

        encodeNode(utils, buf);
    }
}
