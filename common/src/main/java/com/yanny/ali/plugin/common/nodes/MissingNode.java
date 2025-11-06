package com.yanny.ali.plugin.common.nodes;

import com.yanny.ali.Utils;
import com.yanny.ali.api.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class MissingNode implements IDataNode {
    public static final ResourceLocation ID = new ResourceLocation(Utils.MOD_ID, "missing");

    public MissingNode() {
    }

    public MissingNode(IClientUtils utils, FriendlyByteBuf buf) {
    }

    @Override
    public ITooltipNode getTooltip() {
        return EmptyTooltipNode.EMPTY; // FIXME error node?
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void encode(IServerUtils utils, FriendlyByteBuf buf) {

    }
}
