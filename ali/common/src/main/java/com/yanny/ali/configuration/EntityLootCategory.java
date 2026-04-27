package com.yanny.ali.configuration;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

public class EntityLootCategory extends LootCategory<EntityType<?>> {
    private final List<Either<TagKey<EntityType<?>>, EntityType<?>>> entityTypes;

    public EntityLootCategory(ResourceLocation key, Item icon, boolean hide, List<Ingredient> catalysts, List<Either<TagKey<EntityType<?>>, EntityType<?>>> entityTypes) {
        super(key, icon, Type.ENTITY, hide, catalysts);
        this.entityTypes = entityTypes;
    }

    public EntityLootCategory(JsonObject object) {
        super(Type.ENTITY, object);
        entityTypes = GsonHelper.getAsJsonArray(object, "entityTypes")
                .asList()
                .stream()
                .map(JsonElement::getAsString)
                .map((s) -> {
                    if (s.startsWith("#")) {
                        ResourceLocation location = ResourceLocation.tryParse(s.substring(1));

                        if (location != null) {
                            return Either.<TagKey<EntityType<?>>, EntityType<?>>left(TagKey.create(Registries.ENTITY_TYPE, location));
                        }
                    } else {
                        ResourceLocation location = ResourceLocation.tryParse(s);

                        if (location != null && BuiltInRegistries.BLOCK.containsKey(location)) {
                            return Either.<TagKey<EntityType<?>>, EntityType<?>>right(BuiltInRegistries.ENTITY_TYPE.get(location));
                        }
                    }

                    return null;
                })
                .toList();
    }

    @Override
    protected void toJson(JsonObject object) {
        JsonArray array = new JsonArray();

        for (Either<TagKey<EntityType<?>>, EntityType<?>> entry : entityTypes) {
            entry.ifLeft((tag) -> array.add("#" + tag.location()));
            entry.ifRight((entityType) -> array.add(BuiltInRegistries.ENTITY_TYPE.getKey(entityType).toString()));
        }

        object.add("entityTypes", array);
    }

    @Override
    public boolean validate(EntityType<?> entityType) {
        if (entityTypes.isEmpty()) {
            return true;
        }

        return entityTypes.stream().anyMatch((either) -> either.map(
                (tag) -> entityType.builtInRegistryHolder().is(tag),
                (et) -> et == entityType
        ));
    }
}
