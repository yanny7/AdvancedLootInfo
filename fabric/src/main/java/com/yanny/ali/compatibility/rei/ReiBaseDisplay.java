package com.yanny.ali.compatibility.rei;

import com.yanny.ali.compatibility.common.IType;
import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.plugin.TooltipUtils;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.List;

public abstract class ReiBaseDisplay extends BasicDisplay {
    private final LootTable lootEntry;

    public ReiBaseDisplay(List<EntryIngredient> inputs, IType type) {
        super(inputs, TooltipUtils.collectItems(PluginManager.CLIENT_REGISTRY, type.entry()).stream().map(EntryIngredients::of).toList());
        lootEntry = type.entry();
    }

    public LootTable getLootEntry() {
        return lootEntry;
    }
}
