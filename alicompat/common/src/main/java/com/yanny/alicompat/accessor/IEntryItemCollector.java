package com.yanny.alicompat.accessor;

import com.yanny.ali.api.IServerUtils;
import net.minecraft.world.item.Item;

import java.util.List;

public interface IEntryItemCollector {
    List<Item> collectItems(IServerUtils utils);
}
