package com.yanny.ali.plugin.server;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import org.jetbrains.annotations.NotNull;

public class LootFunctionTypes {
    public static final MapCodec<? extends LootItemFunction> UNUSED = create();

    @NotNull
    private static MapCodec<? extends LootItemFunction> create() {
        return MapCodec.unit((LootItemFunction)null);
    }
}
