package com.yanny.alicompat.accessor;

import com.yanny.ali.api.IServerUtils;
import net.minecraft.world.item.ItemStack;

public interface IItemStackModifier {
    ItemStack applyItemStackModifier(IServerUtils utils, ItemStack itemStack);
}
