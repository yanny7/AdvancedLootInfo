package com.yanny.ali.configuration;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

import java.util.List;

public class EntityLootCategory extends LootCategory<EntityType<?>> {
    private final List<Class<?>> classes;

    public EntityLootCategory(ResourceLocation key, Item icon, boolean hide, List<Class<?>> classes) {
        super(key, icon, Type.ENTITY, hide);
        this.classes = classes;
    }

    public EntityLootCategory(JsonObject object) {
        super(Type.ENTITY, object);
        //noinspection unchecked
        classes = (List<Class<?>>) (Object) GsonHelper.getAsJsonArray(object, "classes").asList().stream().map(JsonElement::getAsString).map((String className) -> {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new JsonParseException("Failed to resolve class " + className);
            }
        }).toList();
    }

    @Override
    protected void toJson(JsonObject object) {
        JsonArray array = new JsonArray();

        classes.forEach((c) -> array.add(c.getName()));
        object.add("classes", array);
    }

    @Override
    public boolean validate(EntityType<?> entityType) {
        return classes.stream().anyMatch((p) -> p.isAssignableFrom(entityType.getBaseClass()));
    }
}
