package com.yanny.ali.plugin.mods;

import com.yanny.ali.api.IServerUtils;
import net.minecraft.world.item.Item;

import java.util.List;

public interface IFunctionItemCollector {
    List<Item> collectItems(IServerUtils utils, List<Item> items);
}
