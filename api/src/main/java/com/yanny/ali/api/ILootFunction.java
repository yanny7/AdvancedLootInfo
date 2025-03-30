package com.yanny.ali.api;

import net.minecraft.network.FriendlyByteBuf;

public interface ILootFunction {
    void encode(IContext context, FriendlyByteBuf buf);
}
