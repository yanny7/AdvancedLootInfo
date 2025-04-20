package com.yanny.ali.registries;

import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class GameplayLootCategory extends LootCategory<String> {
    private final List<Pattern> prefix;

    public GameplayLootCategory(String key, ItemStack icon, Type type, List<Pattern> prefix) {
        super(key, icon, type, (path) -> prefix.stream().anyMatch((p) -> p.matcher(path).find()));
        this.prefix = prefix;
    }

    public GameplayLootCategory(String key, ItemStack icon, Type type, Predicate<String> validator) {
        super(key, icon, type, validator);
        this.prefix = List.of();
    }

    public List<Pattern> getPrefix() {
        return prefix;
    }
}
