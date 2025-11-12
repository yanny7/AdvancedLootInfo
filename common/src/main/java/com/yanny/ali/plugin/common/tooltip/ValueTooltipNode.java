package com.yanny.ali.plugin.common.tooltip;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.ITooltipNode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static com.yanny.ali.api.ITooltipNode.pad;

public class ValueTooltipNode extends ListTooltipNode implements ITooltipNode {
    public static final ResourceLocation ID = new ResourceLocation(Utils.MOD_ID, "value");
    private static final LoadingCache<CacheKey, ValueTooltipNode> CACHE = CacheBuilder.newBuilder()
            .build(CacheLoader.from((data) -> data != null ? new ValueTooltipNode(data) : null));

    private final List<String> values;
    private final boolean isKeyValue;
    private final String key;

    private ValueTooltipNode(CacheKey cacheKey) {
        super(cacheKey.children);
        key = cacheKey.key;
        values = cacheKey.values;
        isKeyValue = cacheKey.isKeyValue;
    }

    @Override
    public void encodeNode(FriendlyByteBuf buf) {
        buf.writeInt(values.size());

        for (String value : values) {
            buf.writeUtf(value);
        }

        buf.writeBoolean(isKeyValue);
        buf.writeUtf(key);
    }

    @Override
    public List<Component> getComponents(int pad, boolean showAdvancedTooltip) {
        if (key == null) {
            throw new IllegalStateException("Key was not set! values:" + values);
        }

        List<Component> children = super.getComponents(pad + 1, showAdvancedTooltip);
        List<Component> components = new ArrayList<>(children.size() + 1);

        if (isKeyValue) {
            Component k = transform(values.get(0));
            Component value = transform(values.get(1));
            Component keyValue = Component.translatable("ali.util.advanced_loot_info.key_value", k, value).withStyle(PARAM_STYLE);

            components.add(pad(pad, Component.translatable(key, keyValue).withStyle(TEXT_STYLE)));
        } else {
            Object[] val = values.stream().map(ValueTooltipNode::transform).toArray();

            components.add(pad(pad, Component.translatable(key, val).withStyle(TEXT_STYLE)));
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

        ValueTooltipNode that = (ValueTooltipNode) o;
        return isKeyValue == that.isKeyValue && Objects.equals(values, that.values) && Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), values, isKeyValue, key);
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public String toString() {
        return "ValueTooltipNode{" +
                "key='" + key + '\'' +
                ", values=" + values +
                ", isKeyValue=" + isKeyValue +
                ", children=" + getChildren() +
                '}';
    }

    @NotNull
    public static Builder value(Object... objects) {
        List<String> values;

        if (objects.length == 1) {
            values = Collections.singletonList(objects[0].toString());
        } else {
            values = new ArrayList<>(objects.length);

            for (Object value : objects) {
                values.add(value.toString());
            }
        }

        return new Builder(values);
    }

    @NotNull
    public static Builder keyValue(Object key, Object value) {
        return new Builder(key.toString(), value.toString());
    }

    @NotNull
    public static String translate(String key) {
        return "\uE000" + key;
    }

    @NotNull
    private static Component transform(String v) {
        if (v.charAt(0) == '\uE000') {
            return Component.translatable(v.substring(1)).withStyle(PARAM_STYLE);
        }

        return Component.literal(v).withStyle(PARAM_STYLE);
    }

    @NotNull
    public static ValueTooltipNode decode(IClientUtils utils, FriendlyByteBuf buf) {
        List<ITooltipNode> children = ListTooltipNode.decodeChildren(utils, buf);
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
        String key = buf.readUtf();
        CacheKey cacheKey = new CacheKey(children, key, values, isKeyValue);

        try {
            return CACHE.get(cacheKey);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public static class Builder extends ListTooltipNode.Builder {
        private final List<String> values;
        private final boolean isKeyValue;

        public Builder(List<String> values) {
            this.values = values;
            isKeyValue = false;
        }

        public Builder(String key, String value) {
            values = List.of(key, value);
            isKeyValue = true;
        }

        public ValueTooltipNode build(String key) {
            String internKey = key.intern();
            CacheKey cacheKey = new CacheKey(ImmutableList.copyOf(children), internKey, ImmutableList.copyOf(values), isKeyValue);

            try {
                return CACHE.get(cacheKey);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private record CacheKey(List<ITooltipNode> children, String key, List<String> values, boolean isKeyValue) {
        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            CacheKey cacheKey = (CacheKey) o;
            return isKeyValue == cacheKey.isKeyValue && Objects.equals(key, cacheKey.key) && Objects.equals(values, cacheKey.values) && Objects.equals(children, cacheKey.children);
        }

        @Override
        public int hashCode() {
            return Objects.hash(children, key, values, isKeyValue);
        }
    }
}
