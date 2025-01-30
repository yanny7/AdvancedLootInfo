package com.yanny.advanced_loot_info.api;

import net.minecraft.world.level.storage.loot.LootContext;
import org.jetbrains.annotations.Nullable;

public interface IContext {
    @Nullable LootContext lootContext();

    IRegistry registry();
}
