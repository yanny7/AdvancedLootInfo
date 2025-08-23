package com.yanny.ali.compatibility.rei;

import com.yanny.ali.api.IDataNode;
import com.yanny.ali.compatibility.common.IType;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;

import java.util.List;

public abstract class ReiBaseDisplay extends BasicDisplay {
    private final IDataNode node;

    public ReiBaseDisplay(List<EntryIngredient> inputs, IType type) {
        super(inputs, type.outputs().stream().map(EntryIngredients::of).toList());
        node = type.entry();
    }

    public IDataNode getLootData() {
        return node;
    }
}
