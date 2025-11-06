package com.yanny.ali.api;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ComponentTooltipNode extends ListTooltipNode implements IKeyTooltipNode {
    static {
        registerFactory(ComponentTooltipNode.class, () -> ComponentTooltipNode::new);
    }

    private final List<Component> values;
    private String key = null;
    private boolean translateKey = true;

    private ComponentTooltipNode(Component... values) {
        if (values.length == 1) {
            this.values = Collections.singletonList(values[0]);
        } else {
            this.values = Arrays.asList(values);
        }
    }

    public ComponentTooltipNode(FriendlyByteBuf buf) {
        super(buf);
        int size = buf.readInt();

        if (size == 1) {
            values = Collections.singletonList(buf.readComponent());
        } else {
            values = new ArrayList<>();

            for (int i = 0; i < size; i++) {
                values.add(buf.readComponent());
            }
        }

        key = buf.readNullable(FriendlyByteBuf::readUtf);
    }

    @Override
    public ComponentTooltipNode key(String key) {
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

        for (Component value : values) {
            buf.writeComponent(value);
        }

        buf.writeNullable(key, FriendlyByteBuf::writeUtf);
    }

    @Override
    public List<Component> getComponents(int pad, boolean showAdvancedTooltip) {
        if (key == null) {
            throw new IllegalStateException("Key was not set!");
        }

        if (isAdvancedTooltip() && !showAdvancedTooltip) {
            return Collections.emptyList();
        }


        List<Component> children = super.getComponents(pad + 1, showAdvancedTooltip);
        List<Component> components = new ArrayList<>(children.size() + 1);

        components.add(pad(pad, Component.translatable(key, values.toArray()).withStyle(TEXT_STYLE))); //TODO store as array?
        components.addAll(children);
        return components;
    }

    @NotNull
    public static ComponentTooltipNode value(Component... values) {
        return new ComponentTooltipNode(values);
    }
}
