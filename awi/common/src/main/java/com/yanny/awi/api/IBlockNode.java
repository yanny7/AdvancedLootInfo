package com.yanny.awi.api;

import com.mojang.datafixers.util.Either;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

public interface IBlockNode {
    /** What the slot stands for: one block, or the tag the feature picks a block from. */
    @NotNull
    Either<Block, TagKey<Block>> getBlock();

    /**
     * The blocks this node actually stands for - the single block, or the tag's members resolved against the current
     * registry. Client-side this is what both the rendered slot and the reverse index use, so they cannot disagree.
     */
    @NotNull
    List<Block> getBlocks();

    /**
     * Drops the members the recipe viewer hides, so that {@link #getBlocks()} keeps agreeing with what is rendered. A
     * node standing for a single block has nothing to narrow and can ignore it.
     */
    default void retainBlocks(Predicate<Block> isVisible) {}

    float getChance();
}
