package com.yanny.ali.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.function.Predicate;

public interface ILootModifier<T> {
    boolean predicate(T value);

    IOperation getOperation();

    IType<T> getType();

    enum Operation {
        ADD,
        REPLACE,
        MODIFY,
        REMOVE
    }

    sealed interface IType<T> {
        IType<Block> BLOCK = new BlockType();
        IType<Entity> ENTITY = new EntityType();
        IType<ResourceLocation> LOOT_TABLE = new LootTableType();

        final class BlockType implements IType<Block> {}
        final class EntityType implements IType<Entity> {}
        final class LootTableType implements IType<ResourceLocation> {}
    }

    sealed interface IOperation {
        record AddOperation(Predicate<ItemStack> predicate, IDataNode node) implements IOperation {}
        record RemoveOperation(Predicate<ItemStack> predicate) implements IOperation {}
        record ReplaceOperation(Predicate<ItemStack> predicate, IDataNode node) implements IOperation {}

        Predicate<ItemStack> predicate();
    }
}
