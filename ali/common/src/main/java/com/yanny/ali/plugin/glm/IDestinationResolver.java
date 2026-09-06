package com.yanny.ali.plugin.glm;

import com.yanny.ali.api.IServerUtils;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface IDestinationResolver<T extends LootItemCondition> {
    @Nullable
    Destination resolve(IServerUtils utils, T condition);
}
