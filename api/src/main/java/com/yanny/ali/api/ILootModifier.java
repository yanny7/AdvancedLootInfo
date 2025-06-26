package com.yanny.ali.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;

import java.util.List;

public interface ILootModifier<T> {
    boolean predicate(T value);

    boolean itemPredicate(ItemStack item);

    Operation getOperation();

    LootPool getLootPool();

    Type<T> getType();

    void updateItems(List<Item> items);

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

        final class BlockType implements Type<Block> {}
        final class EntityType implements Type<Entity> {}
        final class LootTableType implements Type<ResourceLocation> {}
    }
}
