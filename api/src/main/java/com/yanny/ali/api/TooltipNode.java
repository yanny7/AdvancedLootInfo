package com.yanny.ali.api;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class TooltipNode implements ITooltipNode {
    private final Component component;
    private final boolean advancedTooltip;
    @Nullable
    private List<ITooltipNode> children = null;

    public TooltipNode() {
        this(Component.empty(), false);
    }

    public TooltipNode(Component component) {
        this(component, false);
    }

    public TooltipNode(Component component, boolean advancedTooltip) {
        this.component = component;
        this.advancedTooltip = advancedTooltip;
    }

    public TooltipNode(RegistryFriendlyByteBuf buf) {
        int count = buf.readInt();

        advancedTooltip = buf.readBoolean();
        component = ComponentSerialization.STREAM_CODEC.decode(buf);


        if (count == 0) {
            children = Collections.emptyList();
        } else if (count == 1) {
            children = Collections.singletonList(new TooltipNode(buf));
        } else {
            children = new ArrayList<>(count);

            for (int i = 0; i < count; i++) {
                children.add(new TooltipNode(buf));
            }
        }
    }

    @Override
    public void add(ITooltipNode node) {
        if (children == null) {
            children = new ArrayList<>();
        }

        children.add(node);
    }

    @Override
    public List<ITooltipNode> getChildren() {
        return Objects.requireNonNullElse(children, Collections.emptyList());
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
        List<ITooltipNode> children = getChildren();

        buf.writeInt(children.size());
        buf.writeBoolean(advancedTooltip);
        ComponentSerialization.STREAM_CODEC.encode(buf, component);

        for (ITooltipNode child : children) {
            child.encode(buf);
        }
    }
}
