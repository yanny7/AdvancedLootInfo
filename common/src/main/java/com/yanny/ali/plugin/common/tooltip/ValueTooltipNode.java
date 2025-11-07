package com.yanny.ali.plugin.common.tooltip;

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

import static com.yanny.ali.api.ITooltipNode.pad;

public class ValueTooltipNode extends ListTooltipNode implements IKeyTooltipNode {
    public static final ResourceLocation ID = new ResourceLocation(Utils.MOD_ID, "value");

    private final List<String> values;
    private final boolean isKeyValue;
    private String key = null;

    private ValueTooltipNode(boolean isKeyValue, Object... values) {
        super(new ArrayList<>());
        this.isKeyValue = isKeyValue;

        if (values.length == 1) {
            this.values = Collections.singletonList(values[0].toString());
        } else {
            this.values = new ArrayList<>(values.length);

            for (Object value : values) {
                this.values.add(value.toString());
            }
        }
    }

    public ValueTooltipNode(List<ITooltipNode> children, @Nullable String key, List<String> values, boolean isKeyValue) {
        super(children);
        this.key = key;
        this.values = values;
        this.isKeyValue = isKeyValue;
    }

    @Override
    public ValueTooltipNode key(String key) {
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
    public void encodeNode(FriendlyByteBuf buf) {
        buf.writeInt(values.size());

        for (String value : values) {
            buf.writeUtf(value);
        }

        buf.writeBoolean(isKeyValue);
        buf.writeNullable(key, FriendlyByteBuf::writeUtf);
    }

    @Override
    public List<Component> getComponents(int pad, boolean showAdvancedTooltip) {
        if (key == null) {
            throw new IllegalStateException("Key was not set! values:" + values);
        }

        List<Component> children = super.getComponents(pad + 1, showAdvancedTooltip);
        List<Component> components = new ArrayList<>(children.size() + 1);

        if (isKeyValue) {
            components.add(pad(pad, Component.translatable(key, Component.translatable("ali.util.advanced_loot_info.key_value", transform(values.get(0)), transform(values.get(1))).withStyle(PARAM_STYLE)).withStyle(TEXT_STYLE)));
        } else {
            Object[] val = values.stream().map(ValueTooltipNode::transform).toArray();

            components.add(pad(pad, Component.translatable(key, val).withStyle(TEXT_STYLE)));
        }

        components.addAll(children);
        return components;
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @NotNull
    public static ValueTooltipNode value(Object... values) {
        return new ValueTooltipNode(false, values);
    }

    @NotNull
    public static ValueTooltipNode keyValue(Object key, Object value) {
        return new ValueTooltipNode(true, key, value);
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
        String key = buf.readNullable(FriendlyByteBuf::readUtf);

        return new ValueTooltipNode(children, key, values, isKeyValue);
    }
}
