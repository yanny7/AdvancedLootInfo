package com.yanny.ali.api;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

import java.util.List;

public interface ITooltipNode {
    List<Component> getComponents(int pad, boolean showAdvancedTooltip);

    boolean isAdvancedTooltip();

    void encode(FriendlyByteBuf buf);
}
