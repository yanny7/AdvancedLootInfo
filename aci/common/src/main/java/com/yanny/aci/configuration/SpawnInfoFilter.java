package com.yanny.aci.configuration;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class SpawnInfoFilter implements Predicate<EntityType<?>> {
    public List<ResourceLocation> entities = new ArrayList<>();
    public boolean whitelist = false;

    public boolean isDisabled() {
        return whitelist && (entities == null || entities.isEmpty());
    }

    @Override
    public boolean test(EntityType<?> type) {
        if (entities == null) {
            return !whitelist;
        }

        return entities.contains(Registry.ENTITY_TYPE.getKey(type)) == whitelist;
    }
}
