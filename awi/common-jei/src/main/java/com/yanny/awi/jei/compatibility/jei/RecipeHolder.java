package com.yanny.awi.jei.compatibility.jei;

import com.yanny.awi.api.IDataNode;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.List;

public final class RecipeHolder {
    private final IDataNode entry;
    private final Identifier id;
    private final List<Block> blocks;
    private JeiWidgetWrapper widgetWrapper;
    private List<JeiBaseLoot.Holder> holders;

    public RecipeHolder(IDataNode entry, Identifier id, List<Block> blocks) {
        this.entry = entry;
        this.id = id;
        this.blocks = blocks;
        widgetWrapper = null;
        holders = null;
    }

    public JeiWidgetWrapper getWidgetWrapper() {
        return widgetWrapper;
    }

    public void setWidgetWrapper(JeiWidgetWrapper widgetWrapper) {
        this.widgetWrapper = widgetWrapper;
    }

    public List<JeiBaseLoot.Holder> getHolders() {
        return holders;
    }

    public void setHolders(List<JeiBaseLoot.Holder> holders) {
        this.holders = holders;
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
