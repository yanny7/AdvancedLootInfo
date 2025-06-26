package com.yanny.ali.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public interface ILootModifier<T> {
    boolean predicate(T value);

    boolean itemPredicate(ItemStack item);

    Operation getOperation();

    IDataNode getNode();

    Type<T> getType();

    enum Operation {
        ADD,
        REPLACE,
        MODIFY,
        REMOVE
    }

    sealed interface Type<T> {
        Type<Block> BLOCK = new BlockType();
        Type<Entity> ENTITY = new EntityType();
        Type<ResourceLocation> LOOT_TABLE = new LootTableType();
        Type<ResourceLocation> TYPE = new TableType();

        final class BlockType implements Type<Block> {}
        final class EntityType implements Type<Entity> {}
        final class LootTableType implements Type<ResourceLocation> {}
        final class TableType implements Type<ResourceLocation> {}
    }
}
