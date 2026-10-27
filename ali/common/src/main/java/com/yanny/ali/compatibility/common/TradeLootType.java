package com.yanny.ali.compatibility.common;

import com.yanny.ali.api.IDataNode;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Set;

public record TradeLootType(Set<Block> pois, Set<Item> accepts, EntityType<?> entityType, IDataNode entry, Identifier id, List<ItemStack> inputs, List<ItemStack> outputs) implements IType {
}
