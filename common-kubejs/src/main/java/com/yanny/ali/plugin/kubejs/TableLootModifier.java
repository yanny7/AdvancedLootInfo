package com.yanny.ali.plugin.kubejs;

import com.almostreliable.lootjs.core.LootModificationByTable;
import com.almostreliable.lootjs.filters.ResourceLocationFilter;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.mixin.MixinLootModificationByTable;
import net.minecraft.resources.ResourceLocation;

public class TableLootModifier extends LootModifier<ResourceLocation> {
    private final ResourceLocationFilter[] tableFilters;

    public TableLootModifier(IServerUtils utils, LootModificationByTable byTable) {
        super(utils, byTable);
        tableFilters = ((MixinLootModificationByTable) byTable).getFilters();
    }

    @Override
    public boolean predicate(ResourceLocation value) {
        for (ResourceLocationFilter tableFilter : tableFilters) {
            if (tableFilter.test(value)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Type<ResourceLocation> getType() {
        return Type.LOOT_TABLE;
    }
}
