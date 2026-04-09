package com.yanny.ali.compatibility.common;

import com.yanny.ali.api.ICommonUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityStorage {
    private static final Map<EntityType<?>, Map<Identifier, Entity>> ENTITIES = new HashMap<>();

    public static void onUnloadLevel() {
        ENTITIES.clear();
    }

    public static Entity getEntity(ICommonUtils utils, EntityType<?> type, Level level, Identifier variant) {
        return ENTITIES.computeIfAbsent(type, (t) -> {
            Map<Identifier, Entity> variantMap = new HashMap<>();

            List<Entity> variants = utils.createEntities(t, level);

            for (Entity entity : variants) {
                if (entity instanceof Mob mob) {
                    mob.getLootTable().ifPresent((location) -> variantMap.put(location.identifier(), entity));
                }
            }

            return variantMap;
        }).get(variant);
    }
}
