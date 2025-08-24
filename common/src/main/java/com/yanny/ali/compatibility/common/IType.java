package com.yanny.ali.compatibility.common;

import com.yanny.ali.api.IDataNode;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface IType {
    IDataNode entry();
    List<ItemStack> inputs();
    List<ItemStack> outputs();
}
