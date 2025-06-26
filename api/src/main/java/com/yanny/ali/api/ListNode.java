package com.yanny.ali.api;

import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

public abstract class ListNode implements IDataNode {
    private final List<IDataNode> nodes;

    public ListNode() {
        nodes = new ArrayList<>();
    }

    public ListNode(IClientUtils utils, FriendlyByteBuf buf) {
        int count = buf.readInt();

        nodes = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            nodes.add(utils.getNodeFactory(buf.readResourceLocation()).create(utils, buf));
        }
    }

    public List<IDataNode> nodes() {
        return nodes;
    }

    public void addChildren(List<ILootModifier<?>> modifiers, IDataNode node) {
        for (ILootModifier<?> modifier : modifiers) {
            switch (modifier.getOperation()) {
                case REPLACE -> {
                }
                case MODIFY -> {
                }
                case REMOVE -> {
                }
            }
        }

        nodes.add(node);
    }

    public abstract void encodeNode(IServerUtils utils, FriendlyByteBuf buf);

    @Override
    public final void encode(IServerUtils utils, FriendlyByteBuf buf) {
        buf.writeInt(nodes.size());

        for (IDataNode node : nodes) {
            buf.writeResourceLocation(node.getId());
            node.encode(utils, buf);
        }

        encodeNode(utils, buf);
    }
}
