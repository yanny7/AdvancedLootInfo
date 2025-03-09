package com.yanny.ali.network;

import com.yanny.ali.api.ICommonUtils;
import com.yanny.ali.api.IContext;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.world.level.storage.loot.LootContext;
import org.jetbrains.annotations.Nullable;

public record AliContext(@Nullable LootContext lootContext, ICommonUtils utils, @Nullable ReloadableServerRegistries.Holder lootDataManager) implements IContext {
}
