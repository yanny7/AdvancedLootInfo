package com.yanny.advanced_loot_info.mixin;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(BlockPredicate.class)
public interface MixinBlockPredicate {
    @Accessor
    TagKey<Block> getTag();

    @Accessor
    Set<Block> getBlocks();

    @Accessor
    StatePropertiesPredicate getProperties();

    @Accessor
    NbtPredicate getNbt();
}
