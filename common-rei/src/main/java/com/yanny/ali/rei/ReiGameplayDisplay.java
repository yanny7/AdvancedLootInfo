package com.yanny.ali.rei;

import com.yanny.ali.compatibility.common.GameplayLootType;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import net.minecraft.resources.Identifier;

import java.util.List;

public class ReiGameplayDisplay extends ReiBaseDisplay {
    private final Identifier id;
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

    public Identifier getId() {
        return id;
    }
}
