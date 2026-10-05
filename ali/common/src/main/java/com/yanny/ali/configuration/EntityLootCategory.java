package com.yanny.ali.configuration;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.yanny.aci.CommonLogUtils;
import com.yanny.ali.Utils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import org.slf4j.Logger;

import java.util.List;
import java.util.Objects;

public class EntityLootCategory extends LootCategory<EntityType<?>> {
    private static final Logger LOGGER = CommonLogUtils.getLogger(Utils.MOD_ID);

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

                        if (location != null && BuiltInRegistries.ENTITY_TYPE.containsKey(location)) {
                            return Either.<TagKey<EntityType<?>>, EntityType<?>>right(BuiltInRegistries.ENTITY_TYPE.get(location));
                        }
                    }

                    LOGGER.warn("Ignoring invalid entry '{}' in 'entityTypes' of category {}", s, getKey());
                    return null;
                })
                .filter(Objects::nonNull)
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
