package com.yanny.advanced_loot_info.compatibility.rei;

import com.yanny.advanced_loot_info.compatibility.common.BlockLootType;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class ReiBlockDisplay extends ReiBaseDisplay {
    private final Block block;
    private final CategoryIdentifier<ReiBlockDisplay> identifier;

    public ReiBlockDisplay(BlockLootType entry, CategoryIdentifier<ReiBlockDisplay> identifier) {
        super(List.of(EntryIngredients.of(entry.block())), entry);
        block = entry.block();
        this.identifier = identifier;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return identifier;
    }

    public Block getBlock() {
        return block;
    }
}
