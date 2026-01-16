package com.yanny.ali.lootjs;

import com.almostreliable.lootjs.core.LootType;
import com.almostreliable.lootjs.core.filters.IdFilter;
import com.almostreliable.lootjs.core.filters.LootTableFilter;
import com.almostreliable.lootjs.loot.modifier.LootModifier;
import com.yanny.ali.api.IServerUtils;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class TableLootModifier extends AbstractLootModifier<Identifier> {
    private final List<Predicate<Identifier>> predicates = new ArrayList<>();

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
    public boolean predicate(Identifier value) {
        return predicates.stream().anyMatch((p) -> p.test(value));
    }

    @Override
    public IType<Identifier> getType() {
        return IType.LOOT_TABLE;
    }
}
