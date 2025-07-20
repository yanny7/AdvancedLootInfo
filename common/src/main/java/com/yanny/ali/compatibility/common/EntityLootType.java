package com.yanny.ali.compatibility.common;

import com.yanny.ali.api.IDataNode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record EntityLootType(Entity entity, IDataNode entry, List<ItemStack> items) implements IType {
}
