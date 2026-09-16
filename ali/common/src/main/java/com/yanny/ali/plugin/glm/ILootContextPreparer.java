package com.yanny.ali.plugin.glm;

import com.yanny.ali.api.IServerUtils;
import net.minecraft.world.level.storage.loot.LootContext;

@FunctionalInterface
public interface ILootContextPreparer {
    void prepare(IServerUtils utils, LootContext context, LootPage page);
}
