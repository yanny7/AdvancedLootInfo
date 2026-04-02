package com.yanny.ali.plugin.server;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

public class LootConditionTypes {
    public static final MapCodec<? extends LootItemCondition> UNUSED = create();

    @NotNull
    private static MapCodec<? extends LootItemCondition> create() {
        return MapCodec.unit((LootItemCondition) null);
    }
}
