package com.yanny.aci.tooltip;

import com.yanny.aci.api.ICoreClientUtils;
import com.yanny.aci.api.ICoreKeyTooltipNode;
import com.yanny.aci.api.ICoreServerUtils;
import com.yanny.aci.api.ICoreTooltipNode;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class CoreListTooltipNode<
        TServerUtils extends ICoreServerUtils<?, ?, ?>,
        TTooltipNode extends ICoreTooltipNode<TServerUtils>
        > implements ICoreTooltipNode<TServerUtils> {
    @Nullable
    private List<TTooltipNode> children;

    protected CoreListTooltipNode(List<TTooltipNode> children) {
        this.children = children;
    }

    protected abstract void encodeNode(RegistryFriendlyByteBuf buf);

    public void addNode(TTooltipNode node) {
        if (children == null) {
            children = new ArrayList<>();
        }

        children.add(node);
    }

    public List<TTooltipNode> getChildren() {
        return Objects.requireNonNullElse(children, Collections.emptyList());
    }

    @NotNull
    @Override
    public List<Component> getComponents(int pad, boolean showAdvancedTooltip) {
        List<TTooltipNode> children = getChildren();
        List<Component> components;
        int count = children.size();

        if (count == 0) {
            components = Collections.emptyList();
        } else if (count == 1) {
            components = children.get(0).getComponents(pad, showAdvancedTooltip);
        } else {
            components = new ArrayList<>(count);

            for (TTooltipNode child : children) {
                components.addAll(child.getComponents(pad, showAdvancedTooltip));
            }
        }

        return components;
    }

    @Override
    public final void encode(TServerUtils utils, RegistryFriendlyByteBuf buf) {
        List<TTooltipNode> children = getChildren();

        buf.writeInt(children.size());

        for (TTooltipNode child : children) {
            ICoreTooltipNode.encodeNode(utils, child, buf);
        }

        encodeNode(buf);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        //noinspection unchecked
        CoreListTooltipNode<TServerUtils, TTooltipNode> that = (CoreListTooltipNode<TServerUtils, TTooltipNode>) o;
        return Objects.equals(children, that.children);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(children);
    }

    public static <
            TTooltipNode extends ICoreTooltipNode<?>,
            TClientUtils extends ICoreClientUtils<TTooltipNode, ?, ?, TClientUtils>
            > List<TTooltipNode> decodeChildren(TClientUtils utils, RegistryFriendlyByteBuf buf) {
        int count = buf.readInt();

        if (count == 0) {
            return Collections.emptyList();
        } else if (count == 1) {
            return Collections.singletonList(ICoreTooltipNode.decodeNode(utils, buf));
        } else {
            List<TTooltipNode> children = new ArrayList<>(count);

            for (int i = 0; i < count; i++) {
                children.add(ICoreTooltipNode.decodeNode(utils, buf));
            }

            return children;
        }
    }

    public abstract static class Builder<
            TTooltipNode    extends ICoreTooltipNode<?>,
            TKeyTooltipNode extends ICoreKeyTooltipNode<?, ?>
            > implements ICoreKeyTooltipNode<TTooltipNode, TKeyTooltipNode> {
        protected final List<TTooltipNode> children = new ArrayList<>();

        @NotNull
        public TKeyTooltipNode add(TTooltipNode node) {
            children.add(node);
            //noinspection unchecked
            return (TKeyTooltipNode) this;
        }
    }
}
