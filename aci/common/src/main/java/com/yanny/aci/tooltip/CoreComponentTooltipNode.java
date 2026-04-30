package com.yanny.aci.tooltip;

import com.google.common.collect.ImmutableList;
import com.yanny.aci.api.ICoreClientUtils;
import com.yanny.aci.api.ICoreKeyTooltipNode;
import com.yanny.aci.api.ICoreServerUtils;
import com.yanny.aci.api.ICoreTooltipNode;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static com.yanny.aci.api.ICoreTooltipNode.pad;

public abstract class CoreComponentTooltipNode<
        TServerUtils extends ICoreServerUtils<?, ?, ?>,
        TTooltipNode extends ICoreTooltipNode<TServerUtils>
        > extends CoreListTooltipNode<TServerUtils, TTooltipNode> implements ICoreTooltipNode<TServerUtils> {
    private final List<Component> values;
    private final String key;

    protected CoreComponentTooltipNode(CacheKey<TTooltipNode> cacheKey) {
        super(cacheKey.children);
        key = cacheKey.key;
        values = cacheKey.values;
    }

    @Override
    protected final void encodeNode(RegistryFriendlyByteBuf buf) {
        buf.writeInt(values.size());

        for (Component value : values) {
            ComponentSerialization.STREAM_CODEC.encode(buf, value);
        }

        buf.writeUtf(key);
    }

    @NotNull
    @Override
    public List<Component> getComponents(int pad, boolean showAdvancedTooltip) {
        if (key == null) {
            throw new IllegalStateException("Key was not set!");
        }

        List<Component> children = super.getComponents(pad + 1, showAdvancedTooltip);
        List<Component> components = new ArrayList<>(children.size() + 1);
        Object[] values = this.values.stream().map(CoreComponentTooltipNode::transform).toArray();

        components.add(pad(pad, Component.translatable(key, values).withStyle(TEXT_STYLE)));
        components.addAll(children);
        return components;
    }

    @Override
    public boolean equals(Object o) {
        if (getClass() != o.getClass()) {
            return false;
        }

        if (!super.equals(o)) {
            return false;
        }

        //noinspection unchecked
        CoreComponentTooltipNode<TServerUtils, TTooltipNode> that = (CoreComponentTooltipNode<TServerUtils, TTooltipNode>) o;
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
    protected static <
            TServerUtils extends ICoreServerUtils<?, ?, ?>,
            TTooltipNode extends ICoreTooltipNode<TServerUtils>,
            TClientUtils extends ICoreClientUtils<TTooltipNode, ?, ?, TClientUtils>,
            SELF         extends CoreComponentTooltipNode<?, ?>
    > SELF decode(TClientUtils utils, RegistryFriendlyByteBuf buf, Function<CacheKey<TTooltipNode>, SELF> factory) {
        List<TTooltipNode> children = CoreListTooltipNode.decodeChildren(utils, buf);
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
        return factory.apply(new CacheKey<>(children, key, values));
    }

    private static Component transform(Component component) {
        if (component instanceof MutableComponent mutableComponent) {
            return mutableComponent.copy().withStyle(ICoreTooltipNode.PARAM_STYLE);
        }

        return component;
    }

    public record CacheKey<TTooltipNode extends ICoreTooltipNode<?>>(List<TTooltipNode> children, String key, List<Component> values) {}

    public static class Builder<
            TServerUtils    extends ICoreServerUtils<?, ?, ?>,
            TTooltipNode    extends ICoreTooltipNode<TServerUtils>,
            SELF            extends CoreComponentTooltipNode<?, ?>,
            TKeyTooltipNode extends ICoreKeyTooltipNode<?, ?>
            > extends CoreListTooltipNode.Builder<TTooltipNode, TKeyTooltipNode> {
        private final List<Component> values;
        private final Function<CacheKey<TTooltipNode>, SELF> factory;

        public Builder(List<Component> values, Function<CacheKey<TTooltipNode>, SELF> factory) {
            this.values = values;
            this.factory = factory;
        }

        @NotNull
        public TTooltipNode build(String key) {
            String internKey = key.intern();
            //noinspection unchecked
            return (TTooltipNode) factory.apply(new CacheKey<>(ImmutableList.copyOf(children), internKey, ImmutableList.copyOf(values)));
        }
    }
}
