package com.yanny.emi_loot_addon.network.function;

import com.yanny.emi_loot_addon.network.LootFunction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import org.jetbrains.annotations.NotNull;

public class UnknownFunction extends LootFunction {

    public UnknownFunction(LootContext lootContext, LootItemFunction function) {
        super(FunctionType.of(function.getType()));
    }

    public UnknownFunction(FunctionType type, @NotNull FriendlyByteBuf buf) {
        super(type);
    }

    @Override
    public void encode(@NotNull FriendlyByteBuf buf) {

    }
}
