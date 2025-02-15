package com.yanny.ali.registries;

import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

public class LootCategory<T> {
    private final String key;
    private final ItemStack icon;
    private final Type type;
    private final Predicate<T> validator;

    public LootCategory(String key, ItemStack icon, Type type, Predicate<T> validator) {
        this.key = key;
        this.icon = icon;
        this.type = type;
        this.validator = validator;
    }

    public String getKey() {
        return key;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public Type getType() {
        return type;
    }

    public boolean validate(T t) {
        return validator.test(t);
    }

    public enum Type {
        BLOCK,
        ENTITY,
        GAMEPLAY
    }
}
