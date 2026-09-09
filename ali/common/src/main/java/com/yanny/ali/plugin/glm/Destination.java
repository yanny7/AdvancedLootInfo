package com.yanny.ali.plugin.glm;

import net.minecraft.advancements.critereon.EntityTypePredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.Collection;
import java.util.function.Predicate;

public sealed interface Destination {
    boolean fullyExplained();

    record Blocks(Collection<Block> blocks, boolean fullyExplained) implements Destination {}

    record Entities(EntityTypePredicate type, boolean fullyExplained) implements Destination {}

    record Table(Predicate<ResourceLocation> matcher, boolean fullyExplained) implements Destination {}
}
