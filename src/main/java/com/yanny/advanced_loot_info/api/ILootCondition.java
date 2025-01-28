package com.yanny.advanced_loot_info.api;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

import java.util.List;

public interface ILootCondition {
    void encode(FriendlyByteBuf buf);

    List<Component> getTooltip(int pad);
}
