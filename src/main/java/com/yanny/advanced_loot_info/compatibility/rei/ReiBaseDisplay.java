package com.yanny.advanced_loot_info.compatibility.rei;

import com.yanny.advanced_loot_info.compatibility.common.IType;
import com.yanny.advanced_loot_info.loot.LootTableEntry;
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
