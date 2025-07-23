package com.yanny.ali.plugin.lootjs;

import com.almostreliable.lootjs.loot.modifier.LootModifier;
import com.yanny.ali.api.IServerUtils;
import net.minecraft.core.HolderSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public class EntityLootModifier extends AbstractLootModifier<Entity> {
    private final HolderSet<EntityType<?>> entityTypes;

    public EntityLootModifier(IServerUtils utils, LootModifier modifier, LootModifier.EntityFiltered entityFiltered) {
        super(utils, modifier);
        entityTypes = entityFiltered.entities();
    }

    @Override
    public boolean predicate(Entity value) {
        return entityTypes.stream().anyMatch((f) -> f.value().equals(value.getType()));
    }

    @Override
    public IType<Entity> getType() {
        return IType.ENTITY;
    }
}
