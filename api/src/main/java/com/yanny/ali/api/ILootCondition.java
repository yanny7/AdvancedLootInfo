package com.yanny.ali.api;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

import java.util.List;

public interface ILootCondition {
    void encode(IContext context, FriendlyByteBuf buf);

    List<Component> getTooltip(int pad);
}
