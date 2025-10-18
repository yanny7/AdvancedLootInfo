package com.yanny.ali.registries;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class LootCategory<T> {
    private final String key;
    private final ItemStack icon;
    private final Type type;

    public LootCategory(String key, ItemStack icon, Type type) {
        this.key = key;
        this.icon = icon;
        this.type = type;
    }

    public LootCategory(ResourceLocation location, JsonElement element) {
        JsonObject jsonObject = GsonHelper.convertToJsonObject(element, location.toString());
        type = LootCategory.Type.valueOf(GsonHelper.getAsString(jsonObject, "type"));
        key = GsonHelper.getAsString(jsonObject, "key");
        icon = new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(GsonHelper.getAsString(jsonObject, "icon"))));
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
        return Objects.hash(key, icon, type);
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

    @NotNull
    public final JsonElement toJson() {
        JsonObject object = new JsonObject();

        object.addProperty("type", type.name());
        object.addProperty("icon", BuiltInRegistries.ITEM.getKey(icon.getItem()).toString());
        object.addProperty("key", key);

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
