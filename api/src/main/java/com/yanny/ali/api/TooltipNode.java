package com.yanny.ali.api;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class TooltipNode implements ITooltipNode {
    private final List<ITooltipNode> children;
    private final Component component;

    public TooltipNode(FriendlyByteBuf buf) {
        int count = buf.readInt();

        children = new ArrayList<>(count);
        component = buf.readComponent();

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
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(children.size());
        buf.writeComponent(component);

        for (ITooltipNode child : children) {
            child.encode(buf);
        }
    }
}
