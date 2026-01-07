package com.yanny.ali.api;

import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

sealed public interface IOperation permits IOperation.AddOperation, IOperation.RemoveOperation, IOperation.ReplaceOperation {
    record AddOperation(Predicate<ItemStack> predicate, IDataNode node) implements IOperation {}

    record RemoveOperation(Predicate<ItemStack> predicate) implements IOperation {}

    record ReplaceOperation(Predicate<ItemStack> predicate, Function<IDataNode, List<IDataNode>> factory) implements IOperation {}

    Predicate<ItemStack> predicate();
}
