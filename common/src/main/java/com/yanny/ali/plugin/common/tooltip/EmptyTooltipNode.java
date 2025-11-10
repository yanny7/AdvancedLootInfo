package com.yanny.ali.plugin.common.tooltip;

import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IKeyTooltipNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.List;

public class EmptyTooltipNode implements ITooltipNode, IKeyTooltipNode {
    public static final EmptyTooltipNode EMPTY = new EmptyTooltipNode();
    public static final ResourceLocation ID = new ResourceLocation(Utils.MOD_ID, "empty");

    @Override
    public void encode(IServerUtils utils, FriendlyByteBuf buf) {

    }

    @Override
    public ResourceLocation getId() {
        return ID;
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

    @Override
    public String toString() {
        return "EmptyTooltipNode";
    }

    public static EmptyTooltipNode decode(IClientUtils ignoredUtils, FriendlyByteBuf ignoredBuf) {
        return EMPTY;
    }
}
