package com.yanny.aci.tooltip;

import com.google.common.collect.ImmutableList;
import com.yanny.aci.api.*;
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

public abstract class CoreComponentTooltipNode<SU extends ICoreServerUtils, TN extends ICoreTooltipNode<SU>> extends CoreListTooltipNode<SU, TN> implements ICoreTooltipNode<SU> {
    private final List<Component> values;
    private final String key;

    protected CoreComponentTooltipNode(CacheKey<SU, TN> cacheKey) {
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
        CoreComponentTooltipNode<SU, TN> that = (CoreComponentTooltipNode<SU, TN>) o;
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
            SU extends ICoreServerUtils,
            TN extends ICoreTooltipNode<SU>,
            DN extends ICoreDataNode<SU, TN>,
            CU extends ICoreClientUtils<SU, TN, DN, CU, WU>,
            WU extends ICoreWidgetUtils<SU, TN, DN>,
            T extends ICoreTooltipNode<SU>
    > T decode(CU utils, RegistryFriendlyByteBuf buf, Function<CacheKey<SU, TN>, T> factory) {
        List<TN> children = CoreListTooltipNode.decodeChildren(utils, buf);
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

    public static class Builder<SU extends ICoreServerUtils, TN extends ICoreTooltipNode<SU>, T extends ICoreTooltipNode<SU>, KTN extends ICoreKeyTooltipNode<SU, TN, KTN>> extends CoreListTooltipNode.Builder<SU, TN, KTN> {
        private final List<Component> values;
        private final Function<CacheKey<SU, TN>, T> factory;

        public Builder(List<Component> values, Function<CacheKey<SU, TN>, T> factory) {
            this.values = values;
            this.factory = factory;
        }

        @NotNull
        public TN build(String key) {
            String internKey = key.intern();
            //noinspection unchecked
            return (TN) factory.apply(new CacheKey<>(ImmutableList.copyOf(children), internKey, ImmutableList.copyOf(values)));
        }
    }

    public record CacheKey<SU extends ICoreServerUtils, TN extends ICoreTooltipNode<SU>>(List<TN> children, String key, List<Component> values) {
    }
}
