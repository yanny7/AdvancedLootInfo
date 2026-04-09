package com.yanny.ali.rei.compatibility.rei;

import com.yanny.ali.compatibility.common.TradeLootType;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ReiTradeDisplay extends ReiBaseDisplay {
    private final String id;
    private final CategoryIdentifier<ReiTradeDisplay> identifier;
    private final Set<Block> pois;
    private final Set<Item> accepts;

    public ReiTradeDisplay(TradeLootType entry, CategoryIdentifier<ReiTradeDisplay> identifier) {
        super(getInputIngredients(entry), entry);
        id = entry.id();
        pois = entry.pois();
        accepts = entry.accepts();
        this.identifier = identifier;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return identifier;
    }

    public String getId() {
        return id;
    }

    public Set<Block> getPois() {
        return pois;
    }

    public Set<Item> getAccepts() {
        return accepts;
    }

    @NotNull
    private static List<EntryIngredient> getInputIngredients(TradeLootType entry) {
        List<EntryIngredient> ingredients = new ArrayList<>();

        ingredients.addAll(entry.inputs().stream().map(EntryIngredients::of).toList());
        ingredients.addAll(entry.pois().stream().map(EntryIngredients::of).toList());
        ingredients.addAll(entry.accepts().stream().map(EntryIngredients::of).toList());
        return ingredients;
    }
}
