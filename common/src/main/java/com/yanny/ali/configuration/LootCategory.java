package com.yanny.ali.configuration;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class LootCategory<T> {
    private final ResourceLocation key;
    private final Item icon;
    private final Type type;
    private final boolean hide;
    private final Ingredient catalyst;

    public LootCategory(ResourceLocation key, Item icon, Type type, boolean hide, Ingredient catalyst) {
        this.key = key;
        this.icon = icon;
        this.type = type;
        this.hide = hide;
        this.catalyst = catalyst;
    }

    public LootCategory(LootCategory.Type type, JsonObject jsonObject) {
        this.type = type;
        key = ResourceLocation.tryParse(GsonHelper.getAsString(jsonObject, "key"));
        icon = BuiltInRegistries.ITEM.get(ResourceLocation.parse(GsonHelper.getAsString(jsonObject, "icon")));
        hide = GsonHelper.getAsBoolean(jsonObject, "hide");

        if (jsonObject.has("catalyst")) {
            catalyst = Ingredient.fromJson(jsonObject.get("catalyst"));
        } else {
            catalyst = Ingredient.EMPTY;
        }
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

    public ResourceLocation getKey() {
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

    public Ingredient getCatalyst() {
        return catalyst;
    }

    @NotNull
    public final JsonElement toJson() {
        JsonObject object = new JsonObject();

        object.addProperty("key", key.toString());
        object.addProperty("type", type.name());
        object.addProperty("icon", BuiltInRegistries.ITEM.getKey(icon).toString());
        object.addProperty("hide", hide);

        if (!catalyst.isEmpty()) {
            object.add("catalyst", catalyst.toJson());
        }

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
