package com.yanny.ali.plugin.glm;

import net.minecraft.advancements.criterion.EntityTypePredicate;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.Collection;

public sealed interface Destination {
    boolean fullyExplained();

    record Blocks(Collection<Block> blocks, boolean fullyExplained) implements Destination {}

    record Entities(EntityTypePredicate type, boolean fullyExplained) implements Destination {}

    record Table(Identifier id, boolean fullyExplained) implements Destination {}
}
