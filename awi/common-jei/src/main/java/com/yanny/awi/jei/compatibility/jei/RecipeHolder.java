package com.yanny.awi.jei.compatibility.jei;

import com.yanny.awi.api.IDataNode;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.List;

public final class RecipeHolder {
    private final IDataNode entry;
    private final Identifier id;
    private final List<Block> blocks;

    public RecipeHolder(IDataNode entry, Identifier id, List<Block> blocks) {
        this.entry = entry;
        this.id = id;
        this.blocks = blocks;
    }

    public IDataNode getEntry() {
        return entry;
    }

    public Identifier getId() {
        return id;
    }

    public List<Block> getBlocks() {
        return blocks;
    }
}
