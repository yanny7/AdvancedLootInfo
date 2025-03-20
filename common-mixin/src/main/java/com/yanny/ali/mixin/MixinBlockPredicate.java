package com.yanny.ali.mixin;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(BlockPredicate.class)
public interface MixinBlockPredicate {
    @Nullable
    @Accessor
    TagKey<Block> getTag();

    @Nullable
    @Accessor
    Set<Block> getBlocks();

    @Accessor
    StatePropertiesPredicate getProperties();

    @Accessor
    NbtPredicate getNbt();
}
