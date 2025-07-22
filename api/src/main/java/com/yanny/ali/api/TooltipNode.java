package com.yanny.ali.api;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

import java.util.ArrayList;
import java.util.List;

public final class TooltipNode implements ITooltipNode {
    public static final TooltipNode EMPTY = new TooltipNode(Component.empty());

    private final List<ITooltipNode> children;
    private final Component component;

    public TooltipNode(RegistryFriendlyByteBuf buf) {
        int count = buf.readInt();

        children = new ArrayList<>(count);
        component = ComponentSerialization.STREAM_CODEC.decode(buf);

        for (int i = 0; i < count; i++) {
            children.add(new TooltipNode(buf));
        }
    }

    public TooltipNode(Component component) {
        children = new ArrayList<>();
        this.component = component;
    }

    public TooltipNode() {
        children = new ArrayList<>();
        component = Component.empty();
    }

    @Override
    public void add(ITooltipNode node) {
        children.add(node);
    }

    @Override
    public List<ITooltipNode> getChildren() {
        return children;
    }

    @Override
    public Component getContent() {
        return component;
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeInt(children.size());
        ComponentSerialization.STREAM_CODEC.encode(buf, component);

        for (ITooltipNode child : children) {
            child.encode(buf);
        }
    }
}
