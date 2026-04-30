package com.yanny.aci.tooltip;

import com.google.common.collect.ImmutableList;
import com.yanny.aci.api.ICoreClientUtils;
import com.yanny.aci.api.ICoreKeyTooltipNode;
import com.yanny.aci.api.ICoreServerUtils;
import com.yanny.aci.api.ICoreTooltipNode;
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

public abstract class CoreValueTooltipNode<
        TServerUtils extends ICoreServerUtils<?, ?, ?>,
        TTooltipNode extends ICoreTooltipNode<TServerUtils>
        > extends CoreListTooltipNode<TServerUtils, TTooltipNode> implements ICoreTooltipNode<TServerUtils> {
    private final List<String> values;
    private final boolean isKeyValue;
    private final boolean translateKey;
    private final String key;

    protected CoreValueTooltipNode(CacheKey<TTooltipNode> cacheKey) {
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
        CoreValueTooltipNode<TServerUtils, TTooltipNode> that = (CoreValueTooltipNode<TServerUtils, TTooltipNode>) o;
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
            TServerUtils extends ICoreServerUtils<?, ?, ?>,
            TTooltipNode extends ICoreTooltipNode<TServerUtils>,
            TClientUtils extends ICoreClientUtils<TTooltipNode, ?, ?, TClientUtils>,
            SELF extends CoreValueTooltipNode<?, ?>
            > SELF decode(TClientUtils utils, RegistryFriendlyByteBuf buf, Function<CacheKey<TTooltipNode>, SELF> factory) {
        List<TTooltipNode> children = CoreListTooltipNode.decodeChildren(utils, buf);
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

    public record CacheKey<TTooltipNode extends ICoreTooltipNode<?>>(List<TTooltipNode> children, String key, List<String> values, boolean isKeyValue, boolean translateKey) { }

    public static class Builder<
            TTooltipNode    extends ICoreTooltipNode<?>,
            TKeyTooltipNode extends ICoreKeyTooltipNode<?, ?>,
            SELF            extends CoreValueTooltipNode<?, ?>
            > extends CoreListTooltipNode.Builder<TTooltipNode, TKeyTooltipNode> {
        private final List<String> values;
        private final boolean isKeyValue;
        private final Function<CacheKey<TTooltipNode>, SELF> factory;

        public Builder(List<String> values, Function<CacheKey<TTooltipNode>, SELF> factory) {
            this.values = values;
            isKeyValue = false;
            this.factory = factory;
        }

        public Builder(String key, String value, Function<CacheKey<TTooltipNode>, SELF> factory) {
            values = List.of(key, value);
            isKeyValue = true;
            this.factory = factory;
        }

        @NotNull
        public TTooltipNode build(String key) {
            //noinspection unchecked
            return (TTooltipNode) build(key, true);
        }

        public SELF build(String key, boolean translateKey) {
            String internKey = key.intern();
            return factory.apply(new CacheKey<>(ImmutableList.copyOf(children), internKey, ImmutableList.copyOf(values), isKeyValue, translateKey));
        }
    }
}
