package com.yanny.awi.rei.compatibility.rei;

import com.yanny.awi.compatibility.BlockGeneration;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;

import java.util.List;

public class ReiBlockGenerationDisplay extends BasicDisplay {
    private final BlockGeneration.Page page;
    private final CategoryIdentifier<ReiBlockGenerationDisplay> identifier;

    public ReiBlockGenerationDisplay(BlockGeneration.Page page, CategoryIdentifier<ReiBlockGenerationDisplay> identifier) {
        super(List.of(), page.blocks().stream().map((block) -> EntryIngredient.of(ReiBaseCategory.toStack(block))).toList());
        this.page = page;
        this.identifier = identifier;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return identifier;
    }

    public BlockGeneration.Page getPage() {
        return page;
    }
}
