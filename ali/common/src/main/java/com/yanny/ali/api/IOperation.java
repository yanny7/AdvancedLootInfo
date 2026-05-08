package com.yanny.ali.api;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

sealed public interface IOperation permits IOperation.AddOperation, IOperation.RemoveOperation, IOperation.ReplaceOperation {
    record AddOperation(Predicate<ItemStack> predicate, IDataNode node) implements IOperation {}

    record RemoveOperation(Predicate<ItemStack> predicate, Function<IDataNode, IDataNode> factory) implements IOperation {}

    record ReplaceOperation(Predicate<ItemStack> predicate, Function<IDataNode, List<IDataNode>> factory) implements IOperation {}

    @NotNull
    Predicate<ItemStack> predicate();
}
