package com.yanny.ali.api;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public interface IDataNode {
    List<ITooltipNode> getTooltip();

    ResourceLocation getId();

    void encode(IServerUtils utils, FriendlyByteBuf buf);
}
