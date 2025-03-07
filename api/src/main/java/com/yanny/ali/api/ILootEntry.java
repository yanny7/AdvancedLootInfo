package com.yanny.ali.api;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ILootEntry {
    @NotNull
    default List<Item> collectItems() {
        return List.of();
    }

    void encode(IContext context, FriendlyByteBuf buf);
}
