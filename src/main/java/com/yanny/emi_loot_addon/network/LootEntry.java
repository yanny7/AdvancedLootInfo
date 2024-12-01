package com.yanny.emi_loot_addon.network;

import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class LootEntry {
    public final List<LootFunction> functions;
    public final List<LootCondition> conditions;

    LootEntry(@NotNull FriendlyByteBuf buf) {
        functions = LootFunction.decode(buf);
        conditions = LootCondition.decode(buf);
    }

    LootEntry(List<LootFunction> functions, List<LootCondition> conditions) {
        this.functions = functions;
        this.conditions = conditions;
    }

    public abstract Type getType();

    public void encode(@NotNull FriendlyByteBuf buf) {
        LootFunction.encode(buf, functions);
        LootCondition.encode(buf, conditions);
    }
}
