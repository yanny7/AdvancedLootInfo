package com.yanny.awi.rei.compatibility.rei;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;

import java.util.List;

public class ReiGameplayDisplay extends ReiBaseDisplay {
    private final RecipeHolder entry;
    private final CategoryIdentifier<ReiGameplayDisplay> identifier;

    public ReiGameplayDisplay(RecipeHolder entry, CategoryIdentifier<ReiGameplayDisplay> identifier) {
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
