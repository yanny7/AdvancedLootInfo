package com.yanny.ali.plugin.glm;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

import java.util.List;
import java.util.function.Supplier;

public record LootPage(ResourceLocation tableId,
                       LootContextParamSet paramSet,
                       List<Block> blocks,
                       List<EntityType<?>> entityTypes,
                       Supplier<List<Entity>> samples) {}
