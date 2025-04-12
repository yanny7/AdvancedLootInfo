package com.yanny.ali.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;

public interface ICommonUtils {
    @Nullable
    LootTable getLootTable(ResourceLocation resourceLocation);
}
