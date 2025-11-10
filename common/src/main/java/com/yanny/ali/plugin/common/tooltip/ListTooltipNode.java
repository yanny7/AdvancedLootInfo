package com.yanny.ali.plugin.common.tooltip;

import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class ListTooltipNode implements ITooltipNode {
    @Nullable
    private List<ITooltipNode> children;

    public ListTooltipNode(List<ITooltipNode> children) {
        this.children = children;
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
    public void encode(IServerUtils utils, FriendlyByteBuf buf) {
        List<ITooltipNode> children = getChildren();

        buf.writeInt(children.size());

        for (ITooltipNode child : children) {
            ITooltipNode.encodeNode(utils, child, buf);
        }

        encodeNode(buf);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ListTooltipNode that = (ListTooltipNode) o;
        return Objects.equals(children, that.children);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(children);
    }

    public static List<ITooltipNode> decodeChildren(IClientUtils utils, FriendlyByteBuf buf) {
        int count = buf.readInt();

        if (count == 0) {
            return Collections.emptyList();
        } else if (count == 1) {
            return Collections.singletonList(ITooltipNode.decodeNode(utils, buf));
        } else {
            List<ITooltipNode> children = new ArrayList<>(count);

            for (int i = 0; i < count; i++) {
                children.add(ITooltipNode.decodeNode(utils, buf));
            }

            return children;
        }
    }
}
