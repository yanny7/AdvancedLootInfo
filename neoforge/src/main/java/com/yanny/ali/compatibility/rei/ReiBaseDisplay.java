package com.yanny.ali.compatibility.rei;

import com.yanny.ali.compatibility.common.IType;
import com.yanny.ali.plugin.entry.LootTableEntry;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;

import java.util.List;

public abstract class ReiBaseDisplay extends BasicDisplay {
    private final LootTableEntry lootEntry;

    public ReiBaseDisplay(List<EntryIngredient> inputs, IType type) {
        super(inputs, type.entry().collectItems().stream().map(EntryIngredients::of).toList());
        lootEntry = type.entry();
    }

    public LootTableEntry getLootEntry() {
        return lootEntry;
    }
}
