package com.yanny.advanced_loot_info.compatibility;

import com.yanny.advanced_loot_info.api.RangeValue;
import com.yanny.advanced_loot_info.loot.GroupType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class ItemGroup {
    public final GroupType type;
    public final List<ItemData> items;
    public final List<ItemGroup> groups;
    @Nullable
    public final RollsHolder rollsHolder;
    @Nullable
    public final WeightHolder weightHolder;

    public ItemGroup(GroupType type, List<ItemGroup> groups) {
        this(type, List.of(), groups, null, null);
    }

    public ItemGroup(GroupType type, List<ItemData> items, List<ItemGroup> groups, @Nullable RollsHolder rollsHolder, @Nullable WeightHolder weightHolder) {
        this.type = type;
        this.items = items;
        this.groups = groups;
        this.rollsHolder = rollsHolder;
        this.weightHolder = weightHolder;
    }

    public ItemGroup optimize() {
        if (items.isEmpty() && groups.size() == 1 && rollsHolder == null && (weightHolder == null || weightHolder.isEmpty())) {
            return groups.get(0).optimize();
        }

        ListIterator<ItemGroup> iterator = groups.listIterator();

        while (iterator.hasNext()) {
            ItemGroup group = iterator.next();
            ItemGroup optimized = group.optimize();

            if (group != optimized) {
                iterator.set(optimized);
            }
        }

        return this;
    }

    @NotNull
    public static List<ItemData> getItems(ItemGroup group) {
        List<ItemData> items = new LinkedList<>(group.items);

        for (ItemGroup itemGroup : group.groups) {
            items.addAll(getItems(itemGroup));
        }

        return items;
    }

    public record RollsHolder(RangeValue rolls, RangeValue bonusRolls) {}

    public record WeightHolder(float chance, int quality) {
        public boolean isEmpty() {
            return Float.isNaN(chance) && quality == 0;
        }
    }
}
