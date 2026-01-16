package com.yanny.ali.plugin.common.tooltip;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.ITooltipNode;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class ArrayTooltipNode extends ListTooltipNode {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(Utils.MOD_ID, "array");
    private static final LoadingCache<CacheKey, ArrayTooltipNode> CACHE = CacheBuilder.newBuilder()
            .build(CacheLoader.from((data) -> data != null ? new ArrayTooltipNode(data) : null));

    private ArrayTooltipNode(CacheKey cacheKey) {
        super(cacheKey.children);
    }

    public ArrayTooltipNode add(ITooltipNode node) {
        super.addNode(node);
        return this;
    }

    @Override
    void encodeNode(RegistryFriendlyByteBuf buf) {
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public String toString() {
        return "ArrayTooltipNode{" +
                "children=" + getChildren() +
                '}';
    }

    @NotNull
    public static ArrayTooltipNode.Builder array() {
        return new Builder();
    }

    @NotNull
    public static ArrayTooltipNode decode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        List<ITooltipNode> children = ListTooltipNode.decodeChildren(utils, buf);
        CacheKey cacheKey = new CacheKey(children);

        try {
            return CACHE.get(cacheKey);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public static class Builder {
        private final List<ITooltipNode> children = new ArrayList<>();

        public Builder add(ITooltipNode node) {
            children.add(node);
            return this;
        }

        public ArrayTooltipNode build() {
            CacheKey cacheKey = new CacheKey(ImmutableList.copyOf(children));

            try {
                return CACHE.get(cacheKey);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private record CacheKey(List<ITooltipNode> children) {
        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            CacheKey cacheKey = (CacheKey) o;
            return Objects.equals(children, cacheKey.children);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(children);
        }
    }
}
