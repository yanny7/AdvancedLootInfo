package com.yanny.advanced_loot_info.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.functions.SetContainerLootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SetContainerLootTable.class)
public interface MixinSetContainerLootTable {
    @Accessor
    BlockEntityType<?> getType();

    @Accessor
    ResourceLocation getName();

    @Accessor
    long getSeed();
}
