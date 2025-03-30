package com.yanny.ali.api;

import net.minecraft.network.FriendlyByteBuf;

public interface ILootCondition {
    void encode(IContext context, FriendlyByteBuf buf);
}
