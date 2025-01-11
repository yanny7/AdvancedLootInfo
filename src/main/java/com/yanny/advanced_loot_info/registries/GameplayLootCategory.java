package com.yanny.advanced_loot_info.registries;

import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class GameplayLootCategory extends LootCategory<String> {
    @Nullable
    private final String prefix;

    public GameplayLootCategory(String key, ItemStack icon, Type type, String prefix) {
        super(key, icon, type, (path) -> path.startsWith(prefix));
        this.prefix = prefix;
    }

    public GameplayLootCategory(String key, ItemStack icon, Type type, Predicate<String> validator) {
        super(key, icon, type, validator);
        this.prefix = "";
    }

    @Nullable
    public String getPrefix() {
        return prefix;
    }
}
