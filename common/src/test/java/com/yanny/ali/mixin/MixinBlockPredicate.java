package com.yanny.ali.mixin;

import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface MixinBlockPredicate {
    @Nullable
    TagKey<Block> getTag();

    @Nullable
    Set<Block> getBlocks();

    StatePropertiesPredicate getProperties();
    NbtPredicate getNbt();
}
