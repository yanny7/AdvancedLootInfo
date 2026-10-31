package com.yanny.ali.compatibility.common;

import com.yanny.ali.api.ICommonUtils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.plugin.common.EntityUtils;
import com.yanny.ali.plugin.common.nodes.EntityLootTableNode;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EntityStorage {
    private static final Map<EntityType<?>, Map<ResourceLocation, Entity>> ENTITIES = new HashMap<>();
    private static final Map<EntityType<?>, Map<ResourceLocation, Entity>> VARIANTS = new HashMap<>();

    public static void onUnloadLevel() {
        ENTITIES.clear();
        VARIANTS.clear();
    }

    @Nullable
    public static Entity getEntity(ICommonUtils utils, EntityType<?> type, Level level, ResourceLocation variant, IDataNode node) {
        List<SynchedEntityData.DataValue<?>> data = node instanceof EntityLootTableNode entityNode ? entityNode.getVariantData() : null;

        if (data != null) {
            Map<ResourceLocation, Entity> variants = VARIANTS.computeIfAbsent(type, (t) -> new HashMap<>());

            if (!variants.containsKey(variant)) {
                variants.put(variant, createVariant(type, level, data));
            }

            Entity entity = variants.get(variant);

            if (entity != null) {
                return entity;
            }
        }

        Map<ResourceLocation, Entity> variants = ENTITIES.computeIfAbsent(type, (t) -> {
            Map<ResourceLocation, Entity> variantMap = new LinkedHashMap<>();

            for (Entity entity : utils.createEntities(t, level)) {
                if (entity instanceof Mob mob) {
                    ResourceLocation entityLootTable = mob.getLootTable();

                    //noinspection ConstantValue - some modded mobs does return null
                    if (entityLootTable != null) {
                        variantMap.put(entityLootTable, entity);
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

    @Nullable
    private static Entity createVariant(EntityType<?> type, Level level, List<SynchedEntityData.DataValue<?>> data) {
        Entity entity = EntityUtils.createEntity(type, level);

        if (entity != null) {
            try {
                entity.getEntityData().assignValues(data);
            } catch (Throwable e) {
                return null;
            }
        }

        return entity;
    }
}
