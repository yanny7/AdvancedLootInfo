package com.yanny.ali.plugin.common.tooltip;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IKeyTooltipNode;
import com.yanny.ali.api.ITooltipNode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class BranchTooltipNode extends ListTooltipNode implements IKeyTooltipNode {
    public static final ResourceLocation ID = new ResourceLocation(Utils.MOD_ID, "branch");
    private static final LoadingCache<CacheKey, BranchTooltipNode> CACHE = CacheBuilder.newBuilder()
            .build(CacheLoader.from(cacheKey -> cacheKey != null ? new BranchTooltipNode(cacheKey) : null));

    private String key;
    private boolean advancedTooltip;

    private BranchTooltipNode(List<ITooltipNode> children) {
        super(children);
    }

    private BranchTooltipNode(CacheKey cacheKey) {
        super(cacheKey.children);
        key = cacheKey.key;
        advancedTooltip = cacheKey.advancedTooltip;
    }

    @Override
    public BranchTooltipNode key(String key) {
        if (this.key == null) {
            this.key = key;
            return this;
        }

        throw new IllegalStateException("Double key write!");
    }

    @Override
    public IKeyTooltipNode add(ITooltipNode node) {
        super.addNode(node);
        return this;
    }

    @Override
    void encodeNode(FriendlyByteBuf buf) {
        buf.writeNullable(key, FriendlyByteBuf::writeUtf);
        buf.writeBoolean(advancedTooltip);
    }

    @Override
    public List<Component> getComponents(int pad, boolean showAdvancedTooltip) {
        if (key == null) {
            throw new IllegalStateException("Key was not set!");
        }

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
    public ResourceLocation getId() {
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
        return advancedTooltip == that.advancedTooltip && Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), key, advancedTooltip);
    }

    @Override
    public String toString() {
        return "BranchTooltipNode{" +
                "key='" + key + '\'' +
                ", advancedTooltip=" + advancedTooltip +
                ", children=" + getChildren() +
                '}';
    }

    @NotNull
    public static BranchTooltipNode branch() {
        return new BranchTooltipNode(new ArrayList<>());
    }

    public static BranchTooltipNode branch(String key) {
        return branch().key(key);
    }

    @NotNull
    public static BranchTooltipNode branch(String key, boolean isAdvancedTooltip) {
        BranchTooltipNode node = branch(key);

        if (isAdvancedTooltip) {
            node.advancedTooltip = true;
        }

        return node;
    }

    @NotNull
    public static BranchTooltipNode decode(IClientUtils utils, FriendlyByteBuf buf) {
        List<ITooltipNode> children = ListTooltipNode.decodeChildren(utils, buf);
        String key = buf.readNullable(FriendlyByteBuf::readUtf);
        boolean advancedTooltip = buf.readBoolean();
        CacheKey cacheKey = new CacheKey(children, key, advancedTooltip);

        try {
            return CACHE.get(cacheKey);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private record CacheKey(List<ITooltipNode> children, @Nullable String key, boolean advancedTooltip) {
        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            CacheKey cacheKey = (CacheKey) o;
            return advancedTooltip == cacheKey.advancedTooltip && Objects.equals(key, cacheKey.key) && Objects.equals(children, cacheKey.children);
        }

        @Override
        public int hashCode() {
            return Objects.hash(children, key, advancedTooltip);
        }
    }
}
