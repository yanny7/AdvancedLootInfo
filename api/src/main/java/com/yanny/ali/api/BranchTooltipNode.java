package com.yanny.ali.api;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BranchTooltipNode extends ListTooltipNode implements IKeyTooltipNode {
    static {
        registerFactory(BranchTooltipNode.class, () -> BranchTooltipNode::new);
    }

    private String key = null;

    private BranchTooltipNode() {

    }

    public BranchTooltipNode(FriendlyByteBuf buf) {
        super(buf);
        key = buf.readNullable(FriendlyByteBuf::readUtf);
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

        components.add(pad(pad, Component.translatable(key).withStyle(TEXT_STYLE)));
        components.addAll(children);
        return components;
    }

    public static BranchTooltipNode branch() {
        return new BranchTooltipNode();
    }

    public static BranchTooltipNode branch(String key) {
        return new BranchTooltipNode().key(key);
    }

    @NotNull
    public static BranchTooltipNode branch(String key, boolean isAdvancedTooltip) {
        BranchTooltipNode node = new BranchTooltipNode().key(key);

        if (isAdvancedTooltip) {
            node.setIsAdvancedTooltip();
        }

        return node;
    }
}
