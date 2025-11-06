package com.yanny.ali.api;

import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public class ArrayTooltipNode extends ListTooltipNode {
    static {
        registerFactory(ArrayTooltipNode.class, () -> ArrayTooltipNode::new);
    }

    private ArrayTooltipNode() {

    }

    public ArrayTooltipNode(FriendlyByteBuf buf) {
        super(buf);
    }

    public ArrayTooltipNode add(ITooltipNode node) {
        super.addNode(node);
        return this;
    }

    @Override
    void encodeNode(FriendlyByteBuf buf) {

    }

    @NotNull
    public static ArrayTooltipNode array() {
        return new ArrayTooltipNode();
    }
}
