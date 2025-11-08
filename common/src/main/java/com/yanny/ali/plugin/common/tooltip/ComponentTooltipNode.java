package com.yanny.ali.plugin.common.tooltip;

import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IKeyTooltipNode;
import com.yanny.ali.api.ITooltipNode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.yanny.ali.api.ITooltipNode.pad;

public class ComponentTooltipNode extends ListTooltipNode implements IKeyTooltipNode {
    public static final ResourceLocation ID = new ResourceLocation(Utils.MOD_ID, "component");

    private final List<Component> values;
    private String key = null;

    private ComponentTooltipNode(Component... values) {
        super(new ArrayList<>());

        if (values.length == 1) {
            this.values = Collections.singletonList(values[0]);
        } else {
            this.values = Arrays.asList(values);
        }
    }

    public ComponentTooltipNode(List<ITooltipNode> children, @Nullable String key, List<Component> values) {
        super(children);
        this.key = key;
        this.values = values;
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

        List<Component> children = super.getComponents(pad + 1, showAdvancedTooltip);
        List<Component> components = new ArrayList<>(children.size() + 1);

        components.add(pad(pad, Component.translatable(key, values.stream().map(ComponentTooltipNode::transform).toArray()).withStyle(TEXT_STYLE))); //TODO store as array?
        components.addAll(children);
        return components;
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @NotNull
    public static ComponentTooltipNode value(Component... values) {
        return new ComponentTooltipNode(values);
    }

    @NotNull
    public static ComponentTooltipNode decode(IClientUtils utils, FriendlyByteBuf buf) {
        List<ITooltipNode> children = ListTooltipNode.decodeChildren(utils, buf);
        int size = buf.readInt();
        List<Component> values;

        if (size == 1) {
            values = Collections.singletonList(buf.readComponent());
        } else {
            values = new ArrayList<>();

            for (int i = 0; i < size; i++) {
                values.add(buf.readComponent());
            }
        }

        String key = buf.readNullable(FriendlyByteBuf::readUtf);
        return new ComponentTooltipNode(children, key, values);
    }

    private static Component transform(Component component) {
        if (component instanceof MutableComponent mutableComponent) {
            return mutableComponent.withStyle(ITooltipNode.PARAM_STYLE);
        }

        return component;
    }
}
