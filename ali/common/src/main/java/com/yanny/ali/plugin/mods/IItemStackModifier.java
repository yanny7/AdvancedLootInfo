package com.yanny.ali.plugin.mods;

import com.yanny.ali.api.IServerUtils;
import net.minecraft.world.item.ItemStack;

public interface IItemStackModifier {
    ItemStack applyItemStackModifier(IServerUtils utils, ItemStack itemStack);
}
