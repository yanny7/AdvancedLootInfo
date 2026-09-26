package com.yanny.awi.jei.compatibility.jei;

import com.yanny.awi.api.IDataNode;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.List;

public final class RecipeHolder {
    private final IDataNode entry;
    private final ResourceLocation id;
    private final List<Block> blocks;

    public RecipeHolder(IDataNode entry, ResourceLocation id, List<Block> blocks) {
        this.entry = entry;
        this.id = id;
        this.blocks = blocks;
    }

    public IDataNode getEntry() {
        return entry;
    }

    public ResourceLocation getId() {
        return id;
    }

    public List<Block> getBlocks() {
        return blocks;
    }
}
