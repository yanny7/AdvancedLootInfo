package com.yanny.ali.plugin.server;

import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.NotNull;

public class LootConditionTypes {
    public static final LootItemConditionType UNUSED = create();

    @NotNull
    private static LootItemConditionType create() {
        return new LootItemConditionType(null);
    }
}
