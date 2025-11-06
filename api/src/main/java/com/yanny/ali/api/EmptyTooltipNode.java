package com.yanny.ali.api;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.List;

public class EmptyTooltipNode extends TooltipNode implements IKeyTooltipNode {
    public static final EmptyTooltipNode EMPTY = new EmptyTooltipNode();
    private static final EmptyTooltipNode _EMPTY = new EmptyTooltipNode();

    static {
        registerFactory(EmptyTooltipNode.class, () -> (buf) -> {
            _EMPTY.decode(buf);
            return EMPTY;
        });
    }

    private EmptyTooltipNode() {

    }

    public EmptyTooltipNode(FriendlyByteBuf buf) {
        super(buf);
    }

    @Override
    public void encodeData(FriendlyByteBuf buf) {

    }

    @Override
    public List<Component> getComponents(int pad, boolean showAdvancedTooltip) {
        return Collections.emptyList();
    }

    @Override
    public IKeyTooltipNode key(String key) {
        return this;
    }

    @Override
    public IKeyTooltipNode add(ITooltipNode node) {
        throw new IllegalStateException("Trying to add children to empty node!");
    }
}
