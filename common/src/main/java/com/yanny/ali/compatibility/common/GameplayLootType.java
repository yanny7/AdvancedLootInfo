package com.yanny.ali.compatibility.common;

import com.yanny.ali.api.IDataNode;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record GameplayLootType(IDataNode entry, ResourceLocation id, List<ItemStack> inputs, List<ItemStack> outputs) implements IType {
}
