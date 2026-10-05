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
    public static Codec<SpawnInfoFilter> codec(String modId) {
        return RecordCodecBuilder.create((instance) ->
            instance.group(
                    ConfigCodecs.field(modId, ConfigCodecs.lenientList(modId, ResourceLocation.CODEC, "entities"), "entities", ArrayList::new).forGetter((c) -> c.entities),
                    ConfigCodecs.field(modId, Codec.BOOL, "whitelist", () -> false).forGetter((c) -> c.whitelist)
            ).apply(instance, (entities, whitelist) -> {
                SpawnInfoFilter filter = new SpawnInfoFilter();

                filter.entities = new ArrayList<>(entities);
                filter.whitelist = whitelist;
                return filter;
            })
        );
    }

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
