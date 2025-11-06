package com.yanny.ali.api;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class ListTooltipNode extends TooltipNode {
    @Nullable
    private List<ITooltipNode> children = null;

    public ListTooltipNode() {
    }

    public ListTooltipNode(FriendlyByteBuf buf) {
        super(buf);
        int count = buf.readInt();

        if (count == 0) {
            children = Collections.emptyList();
        } else if (count == 1) {
            children = Collections.singletonList(decodeNode(buf));
        } else {
            children = new ArrayList<>(count);

            for (int i = 0; i < count; i++) {
                children.add(decodeNode(buf));
            }
        }
    }

    abstract void encodeNode(FriendlyByteBuf buf);

    public void addNode(ITooltipNode node) {
        if (children == null) {
            children = new ArrayList<>();
        }

        children.add(node);
    }

    public List<ITooltipNode> getChildren() {
        return Objects.requireNonNullElse(children, Collections.emptyList());
    }

    @Override
    public List<Component> getComponents(int pad, boolean showAdvancedTooltip) {
        if (isAdvancedTooltip() && !showAdvancedTooltip) {
            return Collections.emptyList();
        }

        List<ITooltipNode> children = getChildren();
        List<Component> components;
        int count = children.size();

        if (count == 0) {
            components = Collections.emptyList();
        } else if (count == 1) {
            components = children.get(0).getComponents(pad, showAdvancedTooltip);
        } else {
            components = new ArrayList<>(count);

            for (ITooltipNode child : children) {
                components.addAll(child.getComponents(pad, showAdvancedTooltip));
            }
        }

        return components;
    }

    @Override
    public void encodeData(FriendlyByteBuf buf) {
        List<ITooltipNode> children = getChildren();

        buf.writeInt(children.size());

        for (ITooltipNode child : children) {
            encodeNode(child, buf);
        }

        encodeNode(buf);
    }
}
