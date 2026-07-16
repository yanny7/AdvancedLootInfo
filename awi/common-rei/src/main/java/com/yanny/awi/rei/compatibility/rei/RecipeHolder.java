package com.yanny.awi.rei.compatibility.rei;

import com.yanny.awi.api.IDataNode;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.List;

public record RecipeHolder(IDataNode entry, ResourceLocation id, List<Block> blocks) {}
