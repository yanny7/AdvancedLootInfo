package com.yanny.ali.configuration;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class LootCategory<T> {
    private final Identifier key;
    private final Item icon;
    private final Type type;
    private final boolean hide;
    @Nullable
    private final Ingredient catalyst;

    public LootCategory(Identifier key, Item icon, Type type, boolean hide, @Nullable Ingredient catalyst) {
        this.key = key;
        this.icon = icon;
        this.type = type;
        this.hide = hide;
        this.catalyst = catalyst;
    }

    public abstract boolean validate(T t);

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()){
            return false;
        }

        LootCategory<?> that = (LootCategory<?>) o;
        return Objects.equals(key, that.key) && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, type);
    }

    public Identifier getKey() {
        return key;
    }

    public Item getIcon() {
        return icon;
    }

    public Type getType() {
        return type;
    }

    public boolean isHidden() {
        return hide;
    }

    @Nullable
    public Ingredient getCatalyst() {
        return catalyst;
    }

    public enum Type {
        BLOCK,
        ENTITY,
        GAMEPLAY,
        TRADE
    }
}
