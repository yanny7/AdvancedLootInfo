package com.yanny.alicompat.accessor;

import com.yanny.ali.api.IServerUtils;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;

import java.util.List;

public interface IEntryChildren {
    List<LootPoolEntryContainer> getEntryChildren(IServerUtils utils);
}
