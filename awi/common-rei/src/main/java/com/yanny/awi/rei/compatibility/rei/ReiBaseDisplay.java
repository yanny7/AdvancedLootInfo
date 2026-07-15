package com.yanny.awi.rei.compatibility.rei;

import com.yanny.awi.api.IDataNode;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;

import java.util.List;

public abstract class ReiBaseDisplay extends BasicDisplay {
    private final IDataNode node;

    public ReiBaseDisplay(List<EntryIngredient> inputs, RecipeHolder type) {
        super(inputs, type.blocks().stream().map(EntryIngredients::of).toList());
        node = type.entry();
    }

    public IDataNode getLootData() {
        return node;
    }
}
