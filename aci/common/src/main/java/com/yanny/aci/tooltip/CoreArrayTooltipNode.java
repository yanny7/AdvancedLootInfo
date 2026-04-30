package com.yanny.aci.tooltip;

import com.google.common.collect.ImmutableList;
import com.yanny.aci.api.ICoreClientUtils;
import com.yanny.aci.api.ICoreServerUtils;
import com.yanny.aci.api.ICoreTooltipNode;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class CoreArrayTooltipNode<
        TServerUtils extends ICoreServerUtils<?, ?, ?>,
        TTooltipNode extends ICoreTooltipNode<TServerUtils>
        > extends CoreListTooltipNode<TServerUtils, TTooltipNode> {
    protected CoreArrayTooltipNode(CacheKey<TTooltipNode> cacheKey) {
        super(cacheKey.children);
    }

    protected final CoreArrayTooltipNode<TServerUtils, TTooltipNode> add(TTooltipNode node) {
        super.addNode(node);
        return this;
    }

    @Override
    protected final void encodeNode(RegistryFriendlyByteBuf buf) {
    }

    @Override
    public String toString() {
        return "ArrayTooltipNode{" +
                "children=" + getChildren() +
                '}';
    }

    @NotNull
    protected static <
            TServerUtils extends ICoreServerUtils<?, ?, ?>,
            TTooltipNode extends ICoreTooltipNode<TServerUtils>,
            TClientUtils extends ICoreClientUtils<TTooltipNode, ?, ?, TClientUtils>,
            SELF         extends CoreArrayTooltipNode<?, ?>
            > SELF decode(TClientUtils utils, RegistryFriendlyByteBuf buf, Function<CacheKey<TTooltipNode>, SELF> factory) {
        List<TTooltipNode> children = CoreListTooltipNode.decodeChildren(utils, buf);
        return factory.apply(new CacheKey<>(children));
    }

    public record CacheKey<TTooltipNode extends ICoreTooltipNode<?>>(List<TTooltipNode> children) {}

    public static class Builder<
            TTooltipNode extends ICoreTooltipNode<?>,
            SELF         extends CoreArrayTooltipNode<?, ?>
            > {
        private final List<TTooltipNode> children = new ArrayList<>();
        private final Function<CacheKey<TTooltipNode>, SELF> factory;

        public Builder(Function<CacheKey<TTooltipNode>, SELF> factory) {
            this.factory = factory;
        }

        public Builder<TTooltipNode, SELF> add(TTooltipNode node) {
            children.add(node);
            return this;
        }

        public SELF build() {
            return factory.apply(new CacheKey<>(ImmutableList.copyOf(children)));
        }
    }
}
