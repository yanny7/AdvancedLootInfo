package com.yanny.ali.compatibility.common;

import com.yanny.ali.api.ICommonUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class EntityStorage {
    private static final Map<EntityType<?>, Map<ResourceLocation, Entity>> ENTITIES = new HashMap<>();

    public static void onUnloadLevel() {
        ENTITIES.clear();
    }

    @Nullable
    public static Entity getEntity(ICommonUtils utils, EntityType<?> type, Level level, ResourceLocation variant) {
        Map<ResourceLocation, Entity> variants = ENTITIES.computeIfAbsent(type, (t) -> {
            Map<ResourceLocation, Entity> variantMap = new LinkedHashMap<>();

            for (Entity entity : utils.createEntities(t, level)) {
                if (entity instanceof Mob mob) {
                    ResourceKey<LootTable> entityLootTable = mob.getLootTable();

                    //noinspection ConstantValue - some modded mobs does return null
                    if (entityLootTable != null) {
                        variantMap.put(entityLootTable.location(), entity);
                    }
                }
            }

            return variantMap;
        });
        Entity entity = variants.get(variant);

        // an entry can exist for a variant table nobody registered a factory for (its owner is found by its table id
        // alone), so fall back to whatever instance of that type there is rather than rendering nothing
        return entity != null ? entity : variants.values().stream().findFirst().orElse(null);
    }
}
