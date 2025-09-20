package com.yanny.ali.plugin.mods;

import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import net.minecraft.world.item.Item;
import oshi.util.tuples.Pair;

import java.util.List;

public interface IItemListing {
    IDataNode getNode(IServerUtils utils, List<ITooltipNode> conditions);

    Pair<List<Item>, List<Item>> collectItems(IServerUtils utils);
}
