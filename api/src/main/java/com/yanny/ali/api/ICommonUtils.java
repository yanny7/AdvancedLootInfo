package com.yanny.ali.api;

import com.mojang.datafixers.util.Either;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;

public interface ICommonUtils {
    @Nullable
    LootTable getLootTable(Either<ResourceKey<LootTable>, LootTable> either);
}
