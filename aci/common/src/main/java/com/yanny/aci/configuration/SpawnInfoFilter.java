package com.yanny.aci.configuration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class SpawnInfoFilter implements Predicate<EntityType<?>> {
    public static final Codec<SpawnInfoFilter> CODEC = RecordCodecBuilder.create((instance) ->
        instance.group(
                ResourceLocation.CODEC.listOf().fieldOf("entities").orElseGet(ArrayList::new).forGetter((c) -> c.entities),
                Codec.BOOL.fieldOf("whitelist").orElse(false).forGetter((c) -> c.whitelist)
        ).apply(instance, (entities, whitelist) -> {
            SpawnInfoFilter filter = new SpawnInfoFilter();

            filter.entities = new ArrayList<>(entities);
            filter.whitelist = whitelist;
            return filter;
        })
    );

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

        return entities.contains(BuiltInRegistries.ENTITY_TYPE.getKey(type)) == whitelist;
    }
}
