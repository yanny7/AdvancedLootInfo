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
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static com.yanny.ali.api.ITooltipNode.pad;

public class ComponentTooltipNode extends ListTooltipNode implements ITooltipNode {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(Utils.MOD_ID, "component");
    private static final LoadingCache<CacheKey, ComponentTooltipNode> CACHE = CacheBuilder.newBuilder()
            .build(CacheLoader.from((data) -> data != null ? new ComponentTooltipNode(data) : null));

    private final List<Component> values;
    private final String key;

    private ComponentTooltipNode(CacheKey cacheKey) {
        super(cacheKey.children);
        key = cacheKey.key;
        values = cacheKey.values;
    }

    @Override
    public void encodeNode(RegistryFriendlyByteBuf buf) {
        buf.writeInt(values.size());

        for (Component value : values) {
            ComponentSerialization.STREAM_CODEC.encode(buf, value);
        }

        buf.writeUtf(key);
    }

    @Override
    public List<Component> getComponents(int pad, boolean showAdvancedTooltip) {
        if (key == null) {
            throw new IllegalStateException("Key was not set!");
        }

        List<Component> children = super.getComponents(pad + 1, showAdvancedTooltip);
        List<Component> components = new ArrayList<>(children.size() + 1);
        Object[] values = this.values.stream().map(ComponentTooltipNode::transform).toArray();

        components.add(pad(pad, Component.translatable(key, values).withStyle(TEXT_STYLE)));
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

        ComponentTooltipNode that = (ComponentTooltipNode) o;
        return Objects.equals(values, that.values) && Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), values, key);
    }

    @Override
    public String toString() {
        return "ComponentTooltipNode{" +
                "key='" + key + '\'' +
                ", values=" + values +
                ", children=" + getChildren() +
                '}';
    }

    @NotNull
    public static Builder values(Component... values) {
        return new Builder(Arrays.asList(values));
    }

    @NotNull
    public static ComponentTooltipNode decode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        List<ITooltipNode> children = ListTooltipNode.decodeChildren(utils, buf);
        int size = buf.readInt();
        List<Component> values;

        if (size == 1) {
            values = Collections.singletonList(ComponentSerialization.STREAM_CODEC.decode(buf));
        } else {
            values = new ArrayList<>();

            for (int i = 0; i < size; i++) {
                values.add(ComponentSerialization.STREAM_CODEC.decode(buf));
            }
        }

        String key = buf.readUtf();
        CacheKey cacheKey = new CacheKey(children, key, values);

        try {
            return CACHE.get(cacheKey);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private static Component transform(Component component) {
        if (component instanceof MutableComponent mutableComponent) {
            return mutableComponent.copy().withStyle(ITooltipNode.PARAM_STYLE);
        }

        return component;
    }

    public static class Builder extends ListTooltipNode.Builder {
        private final List<Component> values;

        public Builder(List<Component> values) {
            this.values = values;
        }

        public ComponentTooltipNode build(String key) {
            String internKey = key.intern();
            CacheKey cacheKey = new CacheKey(ImmutableList.copyOf(children), internKey, ImmutableList.copyOf(values));

            try {
                return CACHE.get(cacheKey);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private record CacheKey(List<ITooltipNode> children, String key, List<Component> values) {
        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            CacheKey cacheKey = (CacheKey) o;
            return Objects.equals(key, cacheKey.key) && Objects.equals(values, cacheKey.values) && Objects.equals(children, cacheKey.children);
        }

        @Override
        public int hashCode() {
            return Objects.hash(children, key, values);
        }
    }
}
