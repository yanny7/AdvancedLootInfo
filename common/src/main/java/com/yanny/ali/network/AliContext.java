package com.yanny.ali.network;

import com.yanny.ali.api.ICommonUtils;
import com.yanny.ali.api.IContext;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootDataManager;
import org.jetbrains.annotations.Nullable;

public record AliContext(@Nullable LootContext lootContext, ICommonUtils utils, @Nullable LootDataManager lootDataManager) implements IContext {
}
