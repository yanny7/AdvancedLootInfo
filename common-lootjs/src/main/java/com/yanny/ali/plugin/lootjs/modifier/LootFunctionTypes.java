package com.yanny.ali.plugin.lootjs.modifier;

import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import org.jetbrains.annotations.NotNull;

public class LootFunctionTypes {
    public static final LootItemFunctionType<?> UNUSED = create();

    @NotNull
    private static LootItemFunctionType<?> create() {
        return new LootItemFunctionType<>(null);
    }
}
