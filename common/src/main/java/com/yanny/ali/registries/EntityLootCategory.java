package com.yanny.ali.registries;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Objects;

public class EntityLootCategory extends LootCategory<EntityType<?>> {
    private final List<Class<?>> classes;

    public EntityLootCategory(ResourceLocation key, ItemStack icon, boolean hide, List<Class<?>> classes) {
        super(key, icon, Type.ENTITY, hide);
        this.classes = classes;
    }

    @Override
    protected void toJson(JsonObject object) {
        JsonArray array = new JsonArray();

        classes.forEach((c) -> array.add(c.getName()));
        object.add("classes", array);
    }

    @Override
    public boolean validate(EntityType<?> entityType) {
        return classes.stream().anyMatch((p) -> p.isAssignableFrom(entityType.getClass()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), classes);
    }
}
