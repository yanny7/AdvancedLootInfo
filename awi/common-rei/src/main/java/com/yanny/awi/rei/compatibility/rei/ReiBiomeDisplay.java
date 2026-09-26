package com.yanny.awi.rei.compatibility.rei;

import com.yanny.awi.compatibility.GenericUtils;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryIngredients;

import java.util.List;

public class ReiBiomeDisplay extends ReiBaseDisplay {
    private final RecipeHolder entry;
    private final CategoryIdentifier<ReiBiomeDisplay> identifier;

    public ReiBiomeDisplay(RecipeHolder entry, CategoryIdentifier<ReiBiomeDisplay> identifier) {
        super(GenericUtils.getSpawnEggs(entry.entry()).stream().map(EntryIngredients::of).toList(), entry);
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
