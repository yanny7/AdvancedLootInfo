package com.yanny.ali.mixin;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.functions.SetContainerLootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SetContainerLootTable.class)
public interface MixinSetContainerLootTable {
    @Accessor
    Holder<BlockEntityType<?>> getType();

    @Accessor
    ResourceKey<LootTable> getName();

    @Accessor
    long getSeed();
}
