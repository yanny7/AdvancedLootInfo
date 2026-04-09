package com.yanny.ali.plugin.common.tooltip;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.ITooltipNode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class BranchTooltipNode extends ListTooltipNode implements ITooltipNode {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(Utils.MOD_ID, "branch");
    private static final LoadingCache<CacheKey, BranchTooltipNode> CACHE = CacheBuilder.newBuilder()
            .build(CacheLoader.from((data) -> data != null ? new BranchTooltipNode(data) : null));

    private final String key;
    private final boolean translateKey;
    private final boolean advancedTooltip;

    private BranchTooltipNode(CacheKey cacheKey) {
        super(cacheKey.children);
        key = cacheKey.key;
        advancedTooltip = cacheKey.advancedTooltip;
        translateKey = cacheKey.translateKey;
    }

    @Override
    void encodeNode(RegistryFriendlyByteBuf buf) {
        buf.writeUtf(key);
        buf.writeBoolean(advancedTooltip);
        buf.writeBoolean(translateKey);
    }

    @Override
    public List<Component> getComponents(int pad, boolean showAdvancedTooltip) {
        if (advancedTooltip && !showAdvancedTooltip) {
            return Collections.emptyList();
        }

        List<Component> children = super.getComponents(pad + 1, showAdvancedTooltip);
        List<Component> components = new ArrayList<>(children.size() + 1);

        components.add(ITooltipNode.pad(pad, Component.translatable(key).withStyle(TEXT_STYLE)));
        components.addAll(children);
        return components;
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public boolean equals(Object o) {
        if (getClass() != o.getClass()) {
            return false;
        }

        if (!super.equals(o)) {
            return false;
        }

        BranchTooltipNode that = (BranchTooltipNode) o;
        return advancedTooltip == that.advancedTooltip
                && translateKey == that.translateKey
                && Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), key, advancedTooltip, translateKey);
    }

    @Override
    public String toString() {
        return "BranchTooltipNode{" +
                "key='" + key + '\'' +
                ", advancedTooltip=" + advancedTooltip +
                ", translateKey=" + translateKey +
                ", children=" + getChildren() +
                '}';
    }

    @NotNull
    public static Builder branch() {
        return new Builder(false);
    }

    @NotNull
    public static Builder branch(boolean isAdvancedTooltip) {
        return new Builder(isAdvancedTooltip);
    }

    @NotNull
    public static BranchTooltipNode decode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        List<ITooltipNode> children = ListTooltipNode.decodeChildren(utils, buf);
        String key = buf.readUtf();
        boolean advancedTooltip = buf.readBoolean();
        boolean translateKey = buf.readBoolean();
        CacheKey cacheKey = new CacheKey(children, key, advancedTooltip, translateKey);

        try {
            return CACHE.get(cacheKey);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public static class Builder extends ListTooltipNode.Builder {
        private final boolean advancedTooltip;

        public Builder(boolean advancedTooltip) {
            this.advancedTooltip = advancedTooltip;
        }

        public BranchTooltipNode build(String key) {
            return build(key, true);
        }

        public BranchTooltipNode build(String key, boolean translateKey) {
            String internKey = key.intern();
            CacheKey cacheKey = new CacheKey(ImmutableList.copyOf(children), internKey, advancedTooltip, translateKey);

            try {
                return CACHE.get(cacheKey);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private record CacheKey(List<ITooltipNode> children, String key, boolean advancedTooltip, boolean translateKey) {
        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            CacheKey cacheKey = (CacheKey) o;
            return advancedTooltip == cacheKey.advancedTooltip
                    && translateKey == cacheKey.translateKey
                    && Objects.equals(key, cacheKey.key)
                    && Objects.equals(children, cacheKey.children);
        }

        @Override
        public int hashCode() {
            return Objects.hash(children, key, advancedTooltip, translateKey);
        }
    }
}
