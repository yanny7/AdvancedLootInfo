package com.yanny.ali.plugin.lootjs;

import com.almostreliable.lootjs.core.LootType;
import com.almostreliable.lootjs.core.filters.IdFilter;
import com.almostreliable.lootjs.core.filters.LootTableFilter;
import com.almostreliable.lootjs.loot.modifier.LootModifier;
import com.yanny.ali.api.IServerUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class TableLootModifier extends AbstractLootModifier<ResourceKey<LootTable>> {
    private final List<Predicate<ResourceLocation>> predicates = new ArrayList<>();

    public TableLootModifier(IServerUtils utils, LootModifier modifier, LootModifier.TableFiltered tableFiltered) {
        super(utils, modifier);

        for (LootTableFilter filter : tableFiltered.filters()) {
            if (filter instanceof LootTableFilter.ByIdFilter(IdFilter idFilter)) {
                predicates.add(idFilter);
            } else if (filter instanceof LootTableFilter.ByLootType(LootType type)) {
                predicates.add(Utils.typePredicate(type));
            }
        }
    }

    public TableLootModifier(IServerUtils utils, LootModifier modifier, LootModifier.TypeFiltered typeFiltered) {
        super(utils, modifier);

        for (LootType type : typeFiltered.types()) {
            predicates.add(Utils.typePredicate(type));
        }
    }

    @Override
    public boolean predicate(ResourceKey<LootTable> value) {
        return predicates.stream().anyMatch((p) -> p.test(value.location()));
    }

    @Override
    public IType<ResourceKey<LootTable>> getType() {
        return IType.LOOT_TABLE;
    }
}
