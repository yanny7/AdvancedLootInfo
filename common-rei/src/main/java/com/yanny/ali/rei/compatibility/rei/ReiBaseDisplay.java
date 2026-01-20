package com.yanny.ali.rei.compatibility.rei;

import com.yanny.ali.api.IDataNode;
import com.yanny.ali.compatibility.common.IType;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import org.jetbrains.annotations.Nullable;

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

    @Nullable
    @Override
    public DisplaySerializer<? extends Display> getSerializer() {
        return null;
    }
}
