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
    private final ResourceLocation key;
    private final ItemStack icon;
    private final Type type;
    private final boolean hide;

    public LootCategory(ResourceLocation key, ItemStack icon, Type type, boolean hide) {
        this.key = key;
        this.icon = icon;
        this.type = type;
        this.hide = hide;
    }

    public LootCategory(ResourceLocation location, JsonElement element) {
        JsonObject jsonObject = GsonHelper.convertToJsonObject(element, location.toString());

        type = LootCategory.Type.valueOf(GsonHelper.getAsString(jsonObject, "type"));
        icon = new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(GsonHelper.getAsString(jsonObject, "icon"))));
        hide = GsonHelper.getAsBoolean(jsonObject, "enabled");
        key = location;
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

    public ResourceLocation getKey() {
        return key;
    }

    public ItemStack getIcon() {
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

        object.addProperty("type", type.name());
        object.addProperty("icon", BuiltInRegistries.ITEM.getKey(icon.getItem()).toString());
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
