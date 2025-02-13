package com.yanny.advanced_loot_info.compatibility.rei;

import com.yanny.advanced_loot_info.compatibility.common.GameplayLootType;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;

import java.util.List;

public class ReiGameplayDisplay extends ReiBaseDisplay {
    private final String id;
    private final CategoryIdentifier<ReiGameplayDisplay> identifier;

    public ReiGameplayDisplay(GameplayLootType entry, CategoryIdentifier<ReiGameplayDisplay> identifier) {
        super(List.of(), entry);
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
