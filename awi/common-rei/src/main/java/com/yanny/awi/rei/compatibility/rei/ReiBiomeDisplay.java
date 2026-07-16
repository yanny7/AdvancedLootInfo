package com.yanny.awi.rei.compatibility.rei;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;

import java.util.List;

public class ReiBiomeDisplay extends ReiBaseDisplay {
    private final RecipeHolder entry;
    private final CategoryIdentifier<ReiBiomeDisplay> identifier;

    public ReiBiomeDisplay(RecipeHolder entry, CategoryIdentifier<ReiBiomeDisplay> identifier) {
        super(List.of(), entry);
        this.entry = entry;
        this.identifier = identifier;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return identifier;
    }

    public RecipeHolder getEntry() {
        return entry;
    }
}
