package com.yanny.aci.tooltip;

import com.google.common.collect.ImmutableList;
import com.yanny.aci.api.*;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class CoreArrayTooltipNode<SU extends ICoreServerUtils, TN extends ICoreTooltipNode<SU>> extends CoreListTooltipNode<SU, TN> {
    protected CoreArrayTooltipNode(CacheKey<SU, TN> cacheKey) {
        super(cacheKey.children);
    }

    protected final CoreArrayTooltipNode<SU, TN> add(TN node) {
        super.addNode(node);
        return this;
    }

    @Override
    protected final void encodeNode(FriendlyByteBuf buf) {
    }

    @Override
    public String toString() {
        return "ArrayTooltipNode{" +
                "children=" + getChildren() +
                '}';
    }

    @NotNull
    protected static <
            SU extends ICoreServerUtils,
            TN extends ICoreTooltipNode<SU>,
            DN extends ICoreDataNode<SU, TN>,
            CU extends ICoreClientUtils<SU, TN, DN, CU, WU>,
            WU extends ICoreWidgetUtils<SU, TN, DN>,
            T extends ICoreTooltipNode<SU>
    > T decode(CU utils, FriendlyByteBuf buf, Function<CacheKey<SU, TN>, T> factory) {
        List<TN> children = CoreListTooltipNode.decodeChildren(utils, buf);
        return factory.apply(new CacheKey<>(children));
    }

    public record CacheKey<SU extends ICoreServerUtils, TN extends ICoreTooltipNode<SU>>(List<TN> children) {}

    public static class Builder<SU extends ICoreServerUtils, TN extends ICoreTooltipNode<SU>, T extends ICoreTooltipNode<SU>> {
        private final List<TN> children = new ArrayList<>();
        private final Function<CacheKey<SU, TN>, T> factory;

        public Builder(Function<CacheKey<SU, TN>, T> factory) {
            this.factory = factory;
        }

        public Builder<SU, TN, T> add(TN node) {
            children.add(node);
            return this;
        }

        public T build() {
            return factory.apply(new CacheKey<>(ImmutableList.copyOf(children)));
        }
    }
}
