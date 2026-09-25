package com.yanny.ali.rei.compatibility.rei;

import com.yanny.ali.compatibility.common.TradeLootType;
import com.yanny.ali.platform.Services;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ReiTradeDisplay extends ReiBaseDisplay {
    private final TradeLootType type;
    private final CategoryIdentifier<ReiTradeDisplay> identifier;

    public ReiTradeDisplay(TradeLootType entry, CategoryIdentifier<ReiTradeDisplay> identifier) {
        super(getInputIngredients(entry), entry);
        type = entry;
        this.identifier = identifier;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return identifier;
    }

    public TradeLootType getType() {
        return type;
    }

    @NotNull
    private static List<EntryIngredient> getInputIngredients(TradeLootType entry) {
        List<EntryIngredient> ingredients = new ArrayList<>();

        ingredients.addAll(entry.inputs().stream().map(EntryIngredients::of).toList());
        ingredients.addAll(entry.pois().stream().map(EntryIngredients::of).toList());
        ingredients.addAll(entry.accepts().stream().map(EntryIngredients::of).toList());

        if (entry.entityType() != null) {
            Services.getPlatform().getSpawnEggItem(entry.entityType()).ifPresent((spawnEgg) -> ingredients.add(EntryIngredients.of(spawnEgg.value())));
        }

        return ingredients;
    }
}
