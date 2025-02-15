package com.yanny.ali.api;

import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootDataManager;
import org.jetbrains.annotations.Nullable;

public interface IContext {
    @Nullable LootContext lootContext();

    ICommonUtils utils();

    @Nullable LootDataManager lootDataManager();
}
