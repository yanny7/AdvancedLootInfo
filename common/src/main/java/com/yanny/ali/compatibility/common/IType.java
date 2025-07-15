package com.yanny.ali.compatibility.common;

import com.yanny.ali.api.IDataNode;
import net.minecraft.world.item.Item;

import java.util.List;

public interface IType {
    IDataNode entry();
    List<Item> items();
}
