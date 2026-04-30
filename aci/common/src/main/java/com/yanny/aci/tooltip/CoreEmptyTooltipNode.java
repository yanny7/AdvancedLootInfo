package com.yanny.aci.tooltip;

import com.yanny.aci.api.ICoreKeyTooltipNode;
import com.yanny.aci.api.ICoreServerUtils;
import com.yanny.aci.api.ICoreTooltipNode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public abstract class CoreEmptyTooltipNode<
        TServerUtils    extends ICoreServerUtils<?, ?, ?>,
        TTooltipNode    extends ICoreTooltipNode<?>,
        TKeyTooltipNode extends ICoreKeyTooltipNode<?, ?>
        > implements ICoreTooltipNode<TServerUtils>, ICoreKeyTooltipNode<TTooltipNode, TKeyTooltipNode> {
    protected CoreEmptyTooltipNode() {}

    @Override
    public final void encode(TServerUtils utils, FriendlyByteBuf buf) {
    }

    @NotNull
    @Override
    public List<Component> getComponents(int pad, boolean showAdvancedTooltip) {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return "EmptyTooltipNode{}";
    }

    @NotNull
    @Override
    public TKeyTooltipNode add(TTooltipNode node) {
        //noinspection unchecked
        return (TKeyTooltipNode) this;
    }

    @NotNull
    @Override
    public TTooltipNode build(String key) {
        //noinspection unchecked
        return (TTooltipNode) this;
    }

    public static class Builder<
            TServerUtils    extends ICoreServerUtils<?, ?, ?>,
            TTooltipNode    extends ICoreTooltipNode<?>,
            TKeyTooltipNode extends ICoreKeyTooltipNode<?, ?>
            > extends CoreListTooltipNode.Builder<TTooltipNode, TKeyTooltipNode> {
        private final CoreEmptyTooltipNode<TServerUtils, TTooltipNode, TKeyTooltipNode> node;

        public Builder(CoreEmptyTooltipNode<TServerUtils, TTooltipNode, TKeyTooltipNode> node) {
            this.node = node;
        }

        @NotNull
        @Override
        public TTooltipNode build(String key) {
            //noinspection unchecked
            return (TTooltipNode) node;
        }

        public TTooltipNode build() {
            //noinspection unchecked
            return (TTooltipNode) node;
        }
    }
}
