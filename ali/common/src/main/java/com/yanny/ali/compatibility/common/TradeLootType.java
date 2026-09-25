package com.yanny.ali.compatibility.common;

import com.yanny.ali.api.IDataNode;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public record TradeLootType(Set<Block> pois, Set<Item> accepts, @Nullable EntityType<?> entityType, IDataNode entry, ResourceLocation id, List<ItemStack> inputs, List<ItemStack> outputs) implements IType {
}
