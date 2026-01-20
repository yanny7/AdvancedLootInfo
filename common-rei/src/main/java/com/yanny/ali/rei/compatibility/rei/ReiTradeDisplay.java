package com.yanny.ali.rei.compatibility.rei;

import com.yanny.ali.compatibility.common.TradeLootType;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryIngredients;

public class ReiTradeDisplay extends ReiBaseDisplay {
    private final String id;
    private final CategoryIdentifier<ReiTradeDisplay> identifier;

    public ReiTradeDisplay(TradeLootType entry, CategoryIdentifier<ReiTradeDisplay> identifier) {
        super(entry.inputs().stream().map(EntryIngredients::of).toList(), entry);
        id = entry.id();
        this.identifier = identifier;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return identifier;
    }

    public String getId() {
        return id;
    }
}
