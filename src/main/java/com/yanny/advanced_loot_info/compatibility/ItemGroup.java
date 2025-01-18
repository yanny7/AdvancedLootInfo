package com.yanny.advanced_loot_info.compatibility;

import com.yanny.advanced_loot_info.network.GroupType;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class ItemGroup {
    public final GroupType type;
    public final List<ItemData> items;
    public final List<ItemGroup> groups;

    public ItemGroup(GroupType type, List<ItemGroup> groups) {
        this.type = type;
        this.items = List.of();
        this.groups = groups;
    }

    public ItemGroup(GroupType type, List<ItemData> items, List<ItemGroup> groups) {
        this.type = type;
        this.items = items;
        this.groups = groups;
    }

    public ItemGroup optimize() {
        if (items.isEmpty() && groups.size() == 1) {
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
}
