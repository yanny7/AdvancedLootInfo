package com.yanny.ali.plugin.common.tooltip;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.ITooltipNode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class ArrayTooltipNode extends ListTooltipNode {
    public static final ResourceLocation ID = new ResourceLocation(Utils.MOD_ID, "array");
    private static final LoadingCache<CacheKey, ArrayTooltipNode> CACHE = CacheBuilder.newBuilder()
            .build(CacheLoader.from(cacheKey -> cacheKey != null ? new ArrayTooltipNode(cacheKey) : null));

    private ArrayTooltipNode(CacheKey cacheKey) {
        super(cacheKey.children);
    }

    public ArrayTooltipNode add(ITooltipNode node) {
        super.addNode(node);
        return this;
    }

    @Override
    void encodeNode(FriendlyByteBuf buf) {
    }

    @Override
    public ResourceLocation getId() {
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
    public static ArrayTooltipNode decode(IClientUtils utils, FriendlyByteBuf buf) {
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
            CacheKey cacheKey = new CacheKey(children);

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
            if (o == null || getClass() != o.getClass()) return false;
            CacheKey cacheKey = (CacheKey) o;
            return Objects.equals(children, cacheKey.children);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(children);
        }
    }
}
