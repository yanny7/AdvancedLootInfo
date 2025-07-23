package com.yanny.ali.api;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.List;

public interface ILootModifier<T> {
    boolean predicate(T value);

    List<IOperation> getOperations();

    IType<T> getType();

    sealed interface IType<T> permits IType.BlockType, IType.EntityType, IType.LootTableType {
        IType<Block> BLOCK = new BlockType();
        IType<Entity> ENTITY = new EntityType();
        IType<ResourceKey<LootTable>> LOOT_TABLE = new LootTableType();

        final class BlockType implements IType<Block> {}
        final class EntityType implements IType<Entity> {}
        final class LootTableType implements IType<ResourceKey<LootTable>> {}
    }

}
