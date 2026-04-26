package com.yanny.aci.tooltip;

import com.google.common.collect.ImmutableList;
import com.yanny.aci.api.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static com.yanny.aci.api.ICoreTooltipNode.pad;

public abstract class CoreValueTooltipNode<SU extends ICoreServerUtils, TN extends ICoreTooltipNode<SU>> extends CoreListTooltipNode<SU, TN> implements ICoreTooltipNode<SU> {
    private final List<String> values;
    private final boolean isKeyValue;
    private final boolean translateKey;
    private final String key;

    protected CoreValueTooltipNode(CacheKey<SU, TN> cacheKey) {
        super(cacheKey.children);
        key = cacheKey.key;
        values = cacheKey.values;
        isKeyValue = cacheKey.isKeyValue;
        translateKey = cacheKey.translateKey;
    }

    @Override
    protected final void encodeNode(RegistryFriendlyByteBuf buf) {
        buf.writeInt(values.size());

        for (String value : values) {
            buf.writeUtf(value);
        }

        buf.writeBoolean(isKeyValue);
        buf.writeBoolean(translateKey);
        buf.writeUtf(key);
    }

    @NotNull
    @Override
    public List<Component> getComponents(int pad, boolean showAdvancedTooltip) {
        if (key == null) {
            throw new IllegalStateException("Key was not set! values:" + values);
        }

        List<Component> children = super.getComponents(pad + 1, showAdvancedTooltip);
        List<Component> components = new ArrayList<>(children.size() + 1);

        if (isKeyValue) {
            Component k = transform(values.get(0)).withStyle(TEXT_STYLE);
            Component value = transform(values.get(1)).withStyle(PARAM_STYLE);
            Component keyValue = Component.translatable("ali.util.advanced_loot_info.key_value", k, value);

            components.add(pad(pad, Component.translatable(key, keyValue).withStyle(TEXT_STYLE)));
        } else {
            Object[] val = values.stream().map(CoreValueTooltipNode::transform).toArray();

            if (translateKey) {
                components.add(pad(pad, Component.translatable(key, val).withStyle(TEXT_STYLE)));
            } else {
                components.add(pad(pad, Component.translatable("ali.util.advanced_loot_info.key_value", key, val[0]).withStyle(TEXT_STYLE)));
            }
        }

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
        CoreValueTooltipNode<SU, TN> that = (CoreValueTooltipNode<SU, TN>) o;
        return isKeyValue == that.isKeyValue
                && translateKey == that.translateKey
                && Objects.equals(values, that.values)
                && Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), values, isKeyValue, key, translateKey);
    }

    @Override
    public String toString() {
        return "ValueTooltipNode{" +
                "key='" + key + '\'' +
                ", values=" + values +
                ", isKeyValue=" + isKeyValue +
                ", translateKey=" + translateKey +
                ", children=" + getChildren() +
                '}';
    }

    @NotNull
    public static String translate(String key) {
        return "\uE000" + key;
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
        List<String> values;

        if (size == 1) {
            values = Collections.singletonList(buf.readUtf());
        } else {
            values = new ArrayList<>();

            for (int i = 0; i < size; i++) {
                values.add(buf.readUtf());
            }
        }

        boolean isKeyValue = buf.readBoolean();
        boolean translateKey = buf.readBoolean();
        String key = buf.readUtf();
        return factory.apply(new CacheKey<>(children, key, values, isKeyValue, translateKey));
    }

    @NotNull
    private static MutableComponent transform(String v) {
        if (!v.isEmpty() && v.charAt(0) == '\uE000') {
            return Component.translatable(v.substring(1)).withStyle(PARAM_STYLE);
        }

        return Component.literal(v).withStyle(PARAM_STYLE);
    }

    public static class Builder<SU extends ICoreServerUtils, TN extends ICoreTooltipNode<SU>, T extends ICoreTooltipNode<SU>, KTN extends ICoreKeyTooltipNode<SU, TN, KTN>> extends CoreListTooltipNode.Builder<SU, TN, KTN> {
        private final List<String> values;
        private final boolean isKeyValue;
        private final Function<CacheKey<SU, TN>, T> factory;

        public Builder(List<String> values, Function<CacheKey<SU, TN>, T> factory) {
            this.values = values;
            isKeyValue = false;
            this.factory = factory;
        }

        public Builder(String key, String value, Function<CacheKey<SU, TN>, T> factory) {
            values = List.of(key, value);
            isKeyValue = true;
            this.factory = factory;
        }

        @NotNull
        public TN build(String key) {
            //noinspection unchecked
            return (TN) build(key, true);
        }

        public T build(String key, boolean translateKey) {
            String internKey = key.intern();
            return factory.apply(new CacheKey<>(ImmutableList.copyOf(children), internKey, ImmutableList.copyOf(values), isKeyValue, translateKey));
        }
    }

    public record CacheKey<SU extends ICoreServerUtils, TN extends ICoreTooltipNode<SU>>(List<TN> children, String key, List<String> values, boolean isKeyValue, boolean translateKey) {
    }
}
