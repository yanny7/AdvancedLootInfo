package com.yanny.ali.api;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;

import java.util.List;

public interface ITooltipNode {
    List<ITooltipNode> getChildren();

    void add(ITooltipNode node);

    Component getContent();

    boolean isAdvancedTooltip();

    void encode(RegistryFriendlyByteBuf buf);
}
