package com.yanny.ali.configuration;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class LootCategory<T> {
    private final Identifier key;
    private final Item icon;
    private final Type type;
    private final boolean hide;

    public LootCategory(Identifier key, Item icon, Type type, boolean hide) {
        this.key = key;
        this.icon = icon;
        this.type = type;
        this.hide = hide;
    }

    public LootCategory(LootCategory.Type type, JsonObject jsonObject) {
        this.type = type;
        key = Identifier.tryParse(GsonHelper.getAsString(jsonObject, "key"));
        icon = BuiltInRegistries.ITEM.getValue(Identifier.parse(GsonHelper.getAsString(jsonObject, "icon")));
        hide = GsonHelper.getAsBoolean(jsonObject, "hide");
    }

    protected abstract void toJson(JsonObject object);

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

    @NotNull
    public final JsonElement toJson() {
        JsonObject object = new JsonObject();

        object.addProperty("key", key.toString());
        object.addProperty("type", type.name());
        object.addProperty("icon", BuiltInRegistries.ITEM.getKey(icon).toString());
        object.addProperty("hide", hide);

        toJson(object);
        return object;
    }

    public enum Type {
        BLOCK,
        ENTITY,
        GAMEPLAY,
        TRADE
    }
}
