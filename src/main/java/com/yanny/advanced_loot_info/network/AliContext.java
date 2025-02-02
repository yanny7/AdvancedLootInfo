package com.yanny.advanced_loot_info.network;

import com.yanny.advanced_loot_info.api.ICommonRegistry;
import com.yanny.advanced_loot_info.api.IContext;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootDataManager;
import org.jetbrains.annotations.Nullable;

public record AliContext(@Nullable LootContext lootContext, ICommonRegistry registry, @Nullable LootDataManager lootDataManager) implements IContext {
}
