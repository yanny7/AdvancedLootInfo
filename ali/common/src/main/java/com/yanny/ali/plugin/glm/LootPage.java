package com.yanny.ali.plugin.glm;

import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.function.Supplier;

public record LootPage(Identifier tableId,
                       ContextKeySet paramSet,
                       List<Block> blocks,
                       List<EntityType<?>> entityTypes,
                       Supplier<List<Entity>> samples) {}
