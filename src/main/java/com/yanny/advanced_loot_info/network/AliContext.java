package com.yanny.advanced_loot_info.network;

import com.yanny.advanced_loot_info.api.IContext;
import com.yanny.advanced_loot_info.api.IRegistry;
import net.minecraft.world.level.storage.loot.LootContext;
import org.jetbrains.annotations.Nullable;

public record AliContext(@Nullable LootContext lootContext, IRegistry registry) implements IContext {
}
