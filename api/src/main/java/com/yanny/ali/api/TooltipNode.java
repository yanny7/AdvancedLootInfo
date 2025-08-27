package com.yanny.ali.api;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

import java.util.ArrayList;
import java.util.List;

public final class TooltipNode implements ITooltipNode {
    private final List<ITooltipNode> children;
    private final Component component;
    private final boolean advancedTooltip;

    public TooltipNode() {
        this(Component.empty(), false);
    }

    public TooltipNode(Component component) {
        this(component, false);
    }

    public TooltipNode(Component component, boolean advancedTooltip) {
        children = new ArrayList<>();
        this.component = component;
        this.advancedTooltip = advancedTooltip;
    }

    public TooltipNode(RegistryFriendlyByteBuf buf) {
        int count = buf.readInt();

        children = new ArrayList<>(count);
        advancedTooltip = buf.readBoolean();
        component = ComponentSerialization.STREAM_CODEC.decode(buf);

        for (int i = 0; i < count; i++) {
            children.add(new TooltipNode(buf));
        }
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
    public boolean isAdvancedTooltip() {
        return advancedTooltip;
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeInt(children.size());
        buf.writeBoolean(advancedTooltip);
        ComponentSerialization.STREAM_CODEC.encode(buf, component);

        for (ITooltipNode child : children) {
            child.encode(buf);
        }
    }
}
