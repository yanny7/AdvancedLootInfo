package com.yanny.ali.api;

import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.world.level.storage.loot.LootContext;
import org.jetbrains.annotations.Nullable;

public interface IContext {
    @Nullable LootContext lootContext();

    ICommonUtils utils();

    @Nullable ReloadableServerRegistries.Holder lootDataManager();
}
