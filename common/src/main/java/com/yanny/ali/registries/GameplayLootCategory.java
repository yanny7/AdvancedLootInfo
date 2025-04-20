package com.yanny.ali.registries;

import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

public class GameplayLootCategory extends LootCategory<String> {
    @Nullable
    private final List<String> prefix;

    public GameplayLootCategory(String key, ItemStack icon, Type type, List<String> prefix) {
        super(key, icon, type, (path) -> prefix.stream().anyMatch(path::startsWith));
        this.prefix = prefix;
    }

    public GameplayLootCategory(String key, ItemStack icon, Type type, Predicate<String> validator) {
        super(key, icon, type, validator);
        this.prefix = List.of();
    }

    @Nullable
    public List<String> getPrefix() {
        return prefix;
    }
}
